package com.valeartola.turnos.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.valeartola.turnos.domain.model.AvailableSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime
) {
    public static AvailableSlotResponse from(AvailableSlot slot) {
        return new AvailableSlotResponse(slot.date(), slot.startTime(), slot.endTime());
    }
}
