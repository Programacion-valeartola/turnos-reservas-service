package com.valeartola.turnos.domain.exception;

/** El proceso de reserva no existe o no pertenece al usuario autenticado. */
public class ReservationNotFoundException extends RuntimeException {

    public static final String CODE = "RESERVATION_PROCESS_NOT_FOUND";

    public ReservationNotFoundException(Long id) {
        super("No existe el proceso de reserva " + id);
    }
}