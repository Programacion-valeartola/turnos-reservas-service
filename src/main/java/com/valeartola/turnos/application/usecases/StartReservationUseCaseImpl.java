package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.exception.UserNotFoundException;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.in.StartReservationUseCase;
import com.valeartola.turnos.domain.ports.out.CatalogProvider;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Inicia una reserva: valida el profesional con el catálogo, registra el proceso
 * del usuario y solicita el hold a la cátedra.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartReservationUseCaseImpl implements StartReservationUseCase {

    /** Rechazos definitivos de la cátedra: el proceso termina en FAILED. */
    private static final Set<CatedraError> HOLD_REJECTIONS = Set.of(
            CatedraError.SLOT_ALREADY_HELD,
            CatedraError.SLOT_ALREADY_RESERVED,
            CatedraError.INVALID_SLOT,
            CatedraError.PROFESSIONAL_DISABLED,
            CatedraError.PROFESSIONAL_NOT_FOUND,
            CatedraError.VALIDATION_ERROR);

    private final UserRepository userRepository;
    private final CatalogProvider catalogProvider;
    private final CatedraAppointmentsGateway catedra;
    private final ReservationProcessRepository processes;

    @Override
    public ReservationProcess start(String login, Long professionalId, LocalDate date, LocalTime startTime) {
        // 1. El dueño del proceso sale del token, nunca del cliente
        User user = userRepository.findByLogin(login)
                .orElseThrow(UserNotFoundException::new);
        // 2. Información vigente del catálogo: si está deshabilitado, no se inicia nada
        catalogProvider.getProfessional(professionalId).ensureBookable();

        // 3. Se registra el proceso antes de llamar a la cátedra
        ReservationProcess process = processes.save(
                ReservationProcess.start(user.id(), professionalId, date, startTime));

        // 4. Hold en la cátedra con un identificador estable del paciente
        try {
            Hold hold = catedra.createHold(professionalId, date, startTime, externalPatientId(user));
            log.info("Hold {} creado para el proceso {}", hold.holdId(), process.id());
            return processes.save(process.withHold(hold));
        } catch (CatedraException e) {
            if (HOLD_REJECTIONS.contains(e.getError())) {
                // Rechazo definitivo: el proceso termina en FAILED y se informa al usuario
                processes.save(process.failed(e.getError().name()));
            }
            // Si la cátedra no respondió, el proceso queda en CREATED para la recuperación (#23)
            throw e;
        }
    }

    /** Identificador estable del usuario ante la cátedra (no expone login ni email). */
    private static String externalPatientId(User user) {
        return "user-" + user.id();
    }
}