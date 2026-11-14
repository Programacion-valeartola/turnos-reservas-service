package com.valeartola.turnos.domain.model;

import java.time.Instant;

/** Respuesta de la confirmación inicial del hold (202): la cátedra pasa a esperar el teléfono. */
public record HoldConfirmation(String reservationProcessId, String status, Instant expiresAt) {
}