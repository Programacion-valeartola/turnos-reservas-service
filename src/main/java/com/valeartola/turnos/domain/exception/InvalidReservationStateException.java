package com.valeartola.turnos.domain.exception;

import com.valeartola.turnos.domain.model.ReservationStatus;

/** La operación no es válida para el estado actual del proceso. */
public class InvalidReservationStateException extends RuntimeException {

    public static final String CODE = "INVALID_RESERVATION_STATE";

    public InvalidReservationStateException(Long id, ReservationStatus status) {
        super("El proceso de reserva " + id + " está en estado " + status + " y no admite esta operación");
    }
}