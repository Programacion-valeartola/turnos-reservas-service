package com.valeartola.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Reserva registrada en la cátedra (de toda la cuenta técnica; turnos filtra por usuario). */
public record RemoteAppointment(
        Long reservationId,
        String reservationProcessId,
        String externalPatientId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        Long professionalId,
        String professionalFirstName,
        String professionalLastName,
        String status,
        Instant cancelledAt
) {
}