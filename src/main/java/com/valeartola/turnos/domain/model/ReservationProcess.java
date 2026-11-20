package com.valeartola.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Proceso local de reserva de un usuario. Sigue la máquina de estados:
 * cada transición valida el estado actual y devuelve una copia con el estado nuevo.
 */
public record ReservationProcess(
        Long id,
        Long userId,
        Long professionalId,
        LocalDate appointmentDate,
        LocalTime startTime,
        ReservationStatus status,
        String holdId,
        String reservationProcessId,
        String requestEventId,
        Long reservationId,
        Instant expiresAt,
        String failureReason
) {

    /** El usuario eligió un turno: el proceso existe, pero todavía no hay hold. */
    public static ReservationProcess start(Long userId, Long professionalId, LocalDate date, LocalTime startTime) {
        return new ReservationProcess(null, userId, professionalId, date, startTime,
                ReservationStatus.CREATED, null, null, null, null, null, null);
    }

    /** CREATED → HELD: la cátedra retuvo el turno. */
    public ReservationProcess withHold(Hold hold) {
        requireStatus(ReservationStatus.CREATED);
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                ReservationStatus.HELD, hold.holdId(), hold.reservationProcessId(), requestEventId,
                reservationId, hold.expiresAt(), failureReason);
    }

    /** CREATED → FAILED: la cátedra rechazó el hold (slot ocupado, inválido, etc.). */
    public ReservationProcess failed(String reason) {
        requireStatus(ReservationStatus.CREATED);
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                ReservationStatus.FAILED, holdId, reservationProcessId, requestEventId,
                reservationId, expiresAt, reason);
    }

    private void requireStatus(ReservationStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Transición inválida: el proceso está en " + status
                    + " y se esperaba " + expected);
        }
    }
}