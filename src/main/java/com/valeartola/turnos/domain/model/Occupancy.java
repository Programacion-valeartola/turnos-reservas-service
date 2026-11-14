package com.valeartola.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Un turno no disponible según la cátedra: CONFIRMED (reservado) o HELD (bloqueado, vence en expiresAt). */
public record Occupancy(LocalDate date, LocalTime startTime, String status, Instant expiresAt) {
}