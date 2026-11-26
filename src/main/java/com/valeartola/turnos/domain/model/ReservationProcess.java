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

    /** HELD → AWAITING_PHONE_REQUEST: la cátedra aceptó la confirmación (202). */
    public ReservationProcess awaitingPhoneRequest() {
        requireStatus(ReservationStatus.HELD);
        return withStatus(ReservationStatus.AWAITING_PHONE_REQUEST, failureReason);
    }

    /** Proceso en curso → EXPIRED. Una reserva confirmada o un proceso finalizado no vencen. */
    public ReservationProcess expired(String reason) {
        requireInProgress();
        return withStatus(ReservationStatus.EXPIRED, reason);
    }

    /** true si la confirmación ya se envió antes: confirmar de nuevo no debe repetir la llamada. */
    public boolean confirmationAlreadySent() {
        return status == ReservationStatus.AWAITING_PHONE_REQUEST
                || status == ReservationStatus.PHONE_REQUESTED
                || status == ReservationStatus.PHONE_SUBMITTED
                || status == ReservationStatus.CONFIRMED;
    }

    private ReservationProcess withStatus(ReservationStatus newStatus, String reason) {
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                newStatus, holdId, reservationProcessId, requestEventId, reservationId, expiresAt, reason);
    }

    /** La cátedra pide (o vuelve a pedir) el teléfono. Se guarda su eventId para responderle (#19). */
    public ReservationProcess phoneRequested(String requestEventId, Instant newExpiresAt) {
        requireOneOf(ReservationStatus.HELD, ReservationStatus.AWAITING_PHONE_REQUEST,
                ReservationStatus.PHONE_REQUESTED);
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                ReservationStatus.PHONE_REQUESTED, holdId, reservationProcessId, requestEventId,
                reservationId, newExpiresAt != null ? newExpiresAt : expiresAt, failureReason);
    }

    /** PHONE_SUBMITTED → PHONE_REQUESTED: la cátedra rechazó el teléfono; se puede reintentar hasta expiresAt. */
    public ReservationProcess phoneRejected(String reason, Instant newExpiresAt) {
        requireOneOf(ReservationStatus.PHONE_SUBMITTED, ReservationStatus.PHONE_REQUESTED);
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                ReservationStatus.PHONE_REQUESTED, holdId, reservationProcessId, requestEventId,
                reservationId, newExpiresAt != null ? newExpiresAt : expiresAt, reason);
    }

    /** La cátedra confirmó la reserva. */
    public ReservationProcess confirmed(Long confirmedReservationId) {
        requireOneOf(ReservationStatus.HELD, ReservationStatus.AWAITING_PHONE_REQUEST,
                ReservationStatus.PHONE_REQUESTED, ReservationStatus.PHONE_SUBMITTED);
        return new ReservationProcess(id, userId, professionalId, appointmentDate, startTime,
                ReservationStatus.CONFIRMED, holdId, reservationProcessId, requestEventId,
                confirmedReservationId, expiresAt, null);
    }

    /** Proceso en curso → INVALID: la cátedra detectó una inconsistencia. */
    public ReservationProcess invalid(String reason) {
        requireInProgress();
        return withStatus(ReservationStatus.INVALID, reason);
    }

    /** CONFIRMED → CANCELLED. */
    public ReservationProcess cancelled(String reason) {
        requireStatus(ReservationStatus.CONFIRMED);
        return withStatus(ReservationStatus.CANCELLED, reason);
    }

    private void requireInProgress() {
        if (status.isFinal() || status == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("El proceso ya no está en curso: " + status);
        }
    }

    private void requireOneOf(ReservationStatus... allowed) {
        for (ReservationStatus s : allowed) {
            if (status == s) {
                return;
            }
        }
        throw new IllegalStateException("Transición inválida desde " + status);
    }

    private void requireStatus(ReservationStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Transición inválida: el proceso está en " + status
                    + " y se esperaba " + expected);
        }
    }
}