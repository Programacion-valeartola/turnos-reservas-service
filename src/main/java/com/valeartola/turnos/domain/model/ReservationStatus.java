package com.valeartola.turnos.domain.model;

/** Estados del proceso de reserva (ver docs/maquina-estados-reservas.md). */
public enum ReservationStatus {
    CREATED,
    HELD,
    AWAITING_PHONE_REQUEST,
    PHONE_REQUESTED,
    PHONE_SUBMITTED,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    INVALID,
    FAILED;

    /** Un estado final no retrocede: los eventos que lleguen después se ignoran. */
    public boolean isFinal() {
        return this == CANCELLED || this == EXPIRED || this == INVALID || this == FAILED;
    }
}