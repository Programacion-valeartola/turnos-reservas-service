package com.valeartola.turnos.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyScheduleTest {

    @Test
    void generaLosTurnosDentroDelHorario() {
        WeeklySchedule schedule = new WeeklySchedule(DayOfWeek.MONDAY,
                LocalTime.of(18, 0), LocalTime.of(21, 0), 30, true);

        assertThat(schedule.slotStartTimes()).containsExactly(
                LocalTime.of(18, 0), LocalTime.of(18, 30), LocalTime.of(19, 0),
                LocalTime.of(19, 30), LocalTime.of(20, 0), LocalTime.of(20, 30));
    }

    @Test
    void noOfreceUnTurnoQueTerminariaDespuesDelFin() {
        WeeklySchedule schedule = new WeeklySchedule(DayOfWeek.MONDAY,
                LocalTime.of(18, 0), LocalTime.of(21, 0), 50, true);

        // 20:30 + 50 min = 21:20, se pasa del horario
        assertThat(schedule.slotStartTimes()).containsExactly(
                LocalTime.of(18, 0), LocalTime.of(18, 50), LocalTime.of(19, 40));
    }
}