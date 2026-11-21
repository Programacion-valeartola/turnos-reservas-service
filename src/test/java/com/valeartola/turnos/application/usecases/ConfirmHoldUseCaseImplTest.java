package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.exception.InvalidReservationStateException;
import com.valeartola.turnos.domain.exception.ReservationNotFoundException;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.ReservationStatus;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfirmHoldUseCaseImplTest {

    private static final User USER = new User(7L, "mechi", "hash", "Mechi", "Artola",
            "mechi@example.com", null, true, "es", Set.of("ROLE_USER"), Instant.now());

    @Mock private UserRepository userRepository;
    @Mock private ReservationProcessRepository processes;
    @Mock private CatedraAppointmentsGateway catedra;

    @InjectMocks
    private ConfirmHoldUseCaseImpl useCase;

    private ReservationProcess held;

    @BeforeEach
    void setUp() {
        held = new ReservationProcess(100L, 7L, 15L, LocalDate.of(2026, 11, 23), LocalTime.of(18, 30),
                ReservationStatus.CREATED, null, null, null, null, null, null)
                .withHold(new Hold("hold-1", "process-1", "HELD", Instant.now().plusSeconds(300)));
        when(userRepository.findByLogin("mechi")).thenReturn(Optional.of(USER));
        when(processes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void confirmaElHoldYQuedaEsperandoElPedidoDeTelefono() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(held));

        ReservationProcess result = useCase.confirm("mechi", 100L);

        assertThat(result.status()).isEqualTo(ReservationStatus.AWAITING_PHONE_REQUEST);
        verify(catedra).confirmHold("hold-1", "user-7", "Mechi", "Artola");
    }

    @Test
    void siLaCatedraDiceQueYaEstabaConfirmadoSeTomaComoExito() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(held));
        when(catedra.confirmHold(any(), any(), any(), any()))
                .thenThrow(new CatedraException(CatedraError.RESERVATION_PROCESS_ALREADY_CONFIRMED, 409));

        ReservationProcess result = useCase.confirm("mechi", 100L);

        assertThat(result.status()).isEqualTo(ReservationStatus.AWAITING_PHONE_REQUEST);
    }

    @Test
    void siElHoldVencioElProcesoQuedaEnExpiredYSeInforma() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(held));
        when(catedra.confirmHold(any(), any(), any(), any()))
                .thenThrow(new CatedraException(CatedraError.HOLD_EXPIRED, 404));

        assertThatThrownBy(() -> useCase.confirm("mechi", 100L)).isInstanceOf(CatedraException.class);

        ArgumentCaptor<ReservationProcess> saved = ArgumentCaptor.forClass(ReservationProcess.class);
        verify(processes).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void siLaCatedraNoRespondeElProcesoSigueEnHeld() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(held));
        when(catedra.confirmHold(any(), any(), any(), any()))
                .thenThrow(new CatedraException(CatedraError.UNAVAILABLE, 503));

        assertThatThrownBy(() -> useCase.confirm("mechi", 100L)).isInstanceOf(CatedraException.class);
        verify(processes, never()).save(any());
    }

    @Test
    void unProcesoAjenoSeInformaComoInexistente() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.confirm("mechi", 100L))
                .isInstanceOf(ReservationNotFoundException.class);
        verifyNoInteractions(catedra);
    }

    @Test
    void siYaSeHabiaConfirmadoNoVuelveALlamarALaCatedra() {
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(held.awaitingPhoneRequest()));

        ReservationProcess result = useCase.confirm("mechi", 100L);

        assertThat(result.status()).isEqualTo(ReservationStatus.AWAITING_PHONE_REQUEST);
        verifyNoInteractions(catedra);
    }

    @Test
    void unProcesoFallidoNoSePuedeConfirmar() {
        ReservationProcess failed = new ReservationProcess(100L, 7L, 15L, LocalDate.of(2026, 11, 23),
                LocalTime.of(18, 30), ReservationStatus.FAILED, null, null, null, null, null, "SLOT_ALREADY_RESERVED");
        when(processes.findByIdAndUserId(100L, 7L)).thenReturn(Optional.of(failed));

        assertThatThrownBy(() -> useCase.confirm("mechi", 100L))
                .isInstanceOf(InvalidReservationStateException.class);
        verifyNoInteractions(catedra);
    }
}