package com.valeartola.turnos.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Un turno libre que se le puede ofrecer al usuario. */
public record AvailableSlot(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime) {
}