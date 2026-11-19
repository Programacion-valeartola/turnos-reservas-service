package com.valeartola.turnos.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Regla semanal de atención de un profesional (dato vigente obtenido del catálogo). */
public record WeeklySchedule(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        boolean enabled
) {

    /**
     * Horas de inicio de cada turno dentro del horario.
     * Un turno solo se ofrece si termina antes o justo en endTime.
     * Se calcula en minutos del día para no pasar de medianoche.
     */
    public List<LocalTime> slotStartTimes() {
        List<LocalTime> starts = new ArrayList<>();
        if (slotDurationMinutes <= 0) {
            return starts;
        }
        int start = startTime.toSecondOfDay() / 60;
        int end = endTime.toSecondOfDay() / 60;
        for (int minute = start; minute + slotDurationMinutes <= end; minute += slotDurationMinutes) {
            starts.add(LocalTime.ofSecondOfDay(minute * 60L));
        }
        return starts;
    }
}