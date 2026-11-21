package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.exception.InvalidReservationStateException;
import com.valeartola.turnos.domain.exception.ReservationNotFoundException;
import com.valeartola.turnos.domain.exception.UserNotFoundException;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.ReservationStatus;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.in.ConfirmHoldUseCase;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Confirma inicialmente el hold de un proceso propio. Es seguro reintentar:
 * si la confirmación ya se había enviado, no se repite y se devuelve el estado actual.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmHoldUseCaseImpl implements ConfirmHoldUseCase {

    private final UserRepository userRepository;
    private final ReservationProcessRepository processes;
    private final CatedraAppointmentsGateway catedra;

    @Override
    public ReservationProcess confirm(String login, Long processId) {
        User user = userRepository.findByLogin(login).orElseThrow(UserNotFoundException::new);

        // 1. Solo procesos propios: uno ajeno se informa igual que uno inexistente
        ReservationProcess process = processes.findByIdAndUserId(processId, user.id())
                .orElseThrow(() -> new ReservationNotFoundException(processId));

        // 2. Idempotencia local: si ya se confirmó antes, no se vuelve a llamar a la cátedra
        if (process.confirmationAlreadySent()) {
            return process;
        }
        if (process.status() != ReservationStatus.HELD) {
            throw new InvalidReservationStateException(processId, process.status());
        }

        // 3. Confirmación en la cátedra
        try {
            catedra.confirmHold(process.holdId(), user.externalPatientId(),
                    nameOrLogin(user.firstName(), user), nameOrLogin(user.lastName(), user));
            log.info("Hold {} confirmado: proceso {} esperando el pedido de teléfono", process.holdId(), processId);
            return processes.save(process.awaitingPhoneRequest());

        } catch (CatedraException e) {
            CatedraError error = e.getError();
            if (error == CatedraError.RESERVATION_PROCESS_ALREADY_CONFIRMED) {
                // Reconciliación: un intento anterior (por ejemplo, con timeout) ya había confirmado
                return processes.save(process.awaitingPhoneRequest());
            }
            if (error == CatedraError.HOLD_EXPIRED || error == CatedraError.HOLD_NOT_FOUND) {
                // El hold venció: se cierra el proceso local y se informa al usuario
                processes.save(process.expired(error.name()));
            }
            // Cátedra no disponible: el proceso sigue en HELD y el usuario puede reintentar
            throw e;
        }
    }

    private static String nameOrLogin(String name, User user) {
        return name == null || name.isBlank() ? user.login() : name;
    }
}