package com.valeartola.turnos.domain.model;

import java.time.Instant;

/** Bloqueo temporal de un turno en la cátedra. Vence en expiresAt; guardarlo localmente no lo extiende. */
public record Hold(String holdId, String reservationProcessId, String status, Instant expiresAt) {
}