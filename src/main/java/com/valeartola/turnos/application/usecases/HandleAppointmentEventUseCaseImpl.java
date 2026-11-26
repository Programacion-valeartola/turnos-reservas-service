package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.model.AppointmentEvent;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.ReservationStatus;
import com.valeartola.turnos.domain.ports.in.HandleAppointmentEventUseCase;
import com.valeartola.turnos.domain.ports.out.ProcessedEventRepository;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aplica a los procesos locales los eventos de la cátedra, según la máquina de estados.
 * Idempotente por eventId; un evento tardío nunca hace retroceder un proceso finalizado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandleAppointmentEventUseCaseImpl implements HandleAppointmentEventUseCase {

    private final ProcessedEventRepository processedEvents;
    private final ReservationProcessRepository processes;

    @Override
    public void handle(AppointmentEvent event) {
        // 1. Idempotencia: Kafka entrega al menos una vez
        if (processedEvents.isProcessed(event.eventId())) {
            log.info("Evento {} ya procesado: se ignora el duplicado", event.eventId());
            return;
        }

        // 2. El proceso al que se refiere el evento
        Optional<ReservationProcess> found = processes.findByReservationProcessId(event.reservationProcessId());
        if (found.isEmpty()) {
            log.warn("Evento {} ({}) para un proceso desconocido {}: se ignora",
                    event.eventId(), event.eventType(), event.reservationProcessId());
            processedEvents.markProcessed(event.eventId(), event.eventType());
            return;
        }
        ReservationProcess process = found.get();

        // 3. Transición según el tipo de evento
        try {
            ReservationProcess updated = apply(process, event);
            if (updated != null) {
                processes.save(updated);
                log.info("Proceso {}: {} → {} por {}", process.id(), process.status(), updated.status(), event.eventType());
            }
        } catch (IllegalStateException e) {
            // Evento tardío o fuera de orden: el estado actual se conserva
            log.info("Evento {} ({}) ignorado para el proceso {} en estado {}",
                    event.eventId(), event.eventType(), process.id(), process.status());
        }

        // 4. Se registra recién después de guardar el resultado
        processedEvents.markProcessed(event.eventId(), event.eventType());
    }

    /** Devuelve el proceso actualizado, o null si el evento no produce cambios. */
    private ReservationProcess apply(ReservationProcess process, AppointmentEvent event) {
        return switch (event.eventType()) {
            case AppointmentEvent.ADDITIONAL_INFORMATION_REQUESTED ->
                    process.phoneRequested(event.eventId(), event.expiresAt());
            case AppointmentEvent.ADDITIONAL_INFORMATION_REJECTED ->
                    process.phoneRejected(event.reason(), event.expiresAt());
            case AppointmentEvent.APPOINTMENT_CONFIRMED ->
                    process.status() == ReservationStatus.CONFIRMED ? null : process.confirmed(event.reservationId());
            case AppointmentEvent.APPOINTMENT_PROCESS_EXPIRED -> process.expired(event.reason());
            case AppointmentEvent.APPOINTMENT_PROCESS_INVALID -> process.invalid(event.reason());
            case AppointmentEvent.APPOINTMENT_CANCELLED ->
                    process.status() == ReservationStatus.CANCELLED ? null : process.cancelled(event.reason());
            default -> {
                log.info("Tipo de evento desconocido {}: se ignora", event.eventType());
                yield null;
            }
        };
    }
}
