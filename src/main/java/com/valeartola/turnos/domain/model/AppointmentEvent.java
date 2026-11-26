package com.valeartola.turnos.domain.model;

import java.time.Instant;

/** Evento de acciones de turnos recibido de la cátedra (solo los datos que usa el dominio). */
public record AppointmentEvent(
        String eventId,
        String eventType,
        String reservationProcessId,
        Long reservationId,
        String reason,
        Instant expiresAt
) {
    public static final String ADDITIONAL_INFORMATION_REQUESTED = "AdditionalInformationRequested";
    public static final String ADDITIONAL_INFORMATION_REJECTED = "AdditionalInformationRejected";
    public static final String APPOINTMENT_CONFIRMED = "AppointmentConfirmed";
    public static final String APPOINTMENT_PROCESS_EXPIRED = "AppointmentProcessExpired";
    public static final String APPOINTMENT_PROCESS_INVALID = "AppointmentProcessInvalid";
    public static final String APPOINTMENT_CANCELLED = "AppointmentCancelled";
}