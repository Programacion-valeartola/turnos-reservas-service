package com.valeartola.turnos.domain.model;

import java.time.Instant;

/** Resultado de cancelar una reserva en la cátedra. */
public record CancellationResult(Long reservationId, String reservationProcessId, String status, Instant cancelledAt) {
}