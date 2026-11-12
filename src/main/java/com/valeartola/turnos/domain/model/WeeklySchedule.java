package com.valeartola.turnos.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Regla semanal de atención de un profesional (dato vigente obtenido del catálogo). */
public record WeeklySchedule(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        boolean enabled
) {
}