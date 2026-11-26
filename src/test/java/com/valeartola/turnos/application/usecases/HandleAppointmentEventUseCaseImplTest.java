package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.model.AppointmentEvent;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.ReservationStatus;
import com.valeartola.turnos.domain.ports.out.ProcessedEventRepository;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HandleAppointmentEventUseCaseImplTest {

    @Mock private ProcessedEventRepository processedEvents;
    @Mock private ReservationProcessRepository processes;

    @InjectMocks
    private HandleAppointmentEventUseCaseImpl useCase;

    private ReservationProcess awaiting;

    @BeforeEach
    void setUp() {
        awaiting = new ReservationProcess(100L, 7L, 15L, LocalDate.of(2026, 11, 30), LocalTime.of(18, 30),
                ReservationStatus.CREATED, null, null, null, null, null, null)
                .withHold(new Hold("hold-1", "process-1", "HELD", Instant.now().plusSeconds(300)))
                .awaitingPhoneRequest();
        when(processedEvents.isProcessed(any())).thenReturn(false);
        when(processes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static AppointmentEvent event(String eventId, String type) {
        return new AppointmentEvent(eventId, type, "process-1", 8251L, "MOTIVO", null);
    }

    private ReservationProcess savedProcess() {
        ArgumentCaptor<ReservationProcess> saved = ArgumentCaptor.forClass(ReservationProcess.class);
        verify(processes).save(saved.capture());
        return saved.getValue();
    }

    @Test
    void elPedidoDeTelefonoPasaAPhoneRequestedYGuardaElEventId() {
        when(processes.findByReservationProcessId("process-1")).thenReturn(Optional.of(awaiting));

        useCase.handle(event("request-event-8a31", AppointmentEvent.ADDITIONAL_INFORMATION_REQUESTED));

        ReservationProcess saved = savedProcess();
        assertThat(saved.status()).isEqualTo(ReservationStatus.PHONE_REQUESTED);
        assertThat(saved.requestEventId()).isEqualTo("request-event-8a31");
        verify(processedEvents).markProcessed("request-event-8a31", AppointmentEvent.ADDITIONAL_INFORMATION_REQUESTED);
    }

    @Test
    void laConfirmacionGuardaElIdDeLaReserva() {
        ReservationProcess requested = awaiting.phoneRequested("request-event-8a31", null);
        when(processes.findByReservationProcessId("process-1")).thenReturn(Optional.of(requested));

        useCase.handle(event("confirmation-event-7d10", AppointmentEvent.APPOINTMENT_CONFIRMED));

        ReservationProcess saved = savedProcess();
        assertThat(saved.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.reservationId()).isEqualTo(8251L);
    }

    @Test
    void unEventoRepetidoNoProduceEfectos() {
        when(processedEvents.isProcessed("confirmation-event-7d10")).thenReturn(true);

        useCase.handle(event("confirmation-event-7d10", AppointmentEvent.APPOINTMENT_CONFIRMED));

        verify(processes, never()).findByReservationProcessId(any());
        verify(processes, never()).save(any());
    }

    @Test
    void unEventoTardioNoReabreUnProcesoFinalizado() {
        ReservationProcess expired = awaiting.expired("HOLD_EXPIRED");
        when(processes.findByReservationProcessId("process-1")).thenReturn(Optional.of(expired));

        useCase.handle(event("confirmation-event-7d10", AppointmentEvent.APPOINTMENT_CONFIRMED));

        verify(processes, never()).save(any());
        // Se registra igual, para no volver a evaluarlo
        verify(processedEvents).markProcessed("confirmation-event-7d10", AppointmentEvent.APPOINTMENT_CONFIRMED);
    }

    @Test
    void unEventoDeUnProcesoDesconocidoSeIgnora() {
        when(processes.findByReservationProcessId("process-1")).thenReturn(Optional.empty());

        useCase.handle(event("process-event-7d12", AppointmentEvent.APPOINTMENT_PROCESS_EXPIRED));

        verify(processes, never()).save(any());
        verify(processedEvents).markProcessed("process-event-7d12", AppointmentEvent.APPOINTMENT_PROCESS_EXPIRED);
    }
}