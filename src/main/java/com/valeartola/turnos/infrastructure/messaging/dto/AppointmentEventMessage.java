package com.valeartola.turnos.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.turnos.domain.model.AppointmentEvent;

import java.time.Instant;

/** Evento de acciones de turnos tal como llega por Kafka. Los campos desconocidos se ignoran. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentEventMessage(
        String eventId,
        String eventType,
        String reservationProcessId,
        Long reservationId,
        String reason,
        Instant expiresAt
) {
    public boolean isValid() {
        return eventId != null && !eventId.isBlank()
                && eventType != null && !eventType.isBlank()
                && reservationProcessId != null && !reservationProcessId.isBlank();
    }

    public AppointmentEvent toDomain() {
        return new AppointmentEvent(eventId, eventType, reservationProcessId, reservationId, reason, expiresAt);
    }
}