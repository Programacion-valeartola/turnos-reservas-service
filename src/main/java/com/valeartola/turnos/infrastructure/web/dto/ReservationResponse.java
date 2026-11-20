package com.valeartola.turnos.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.valeartola.turnos.domain.model.ReservationProcess;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        Long id,
        Long professionalId,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        String status,
        String holdId,
        Instant expiresAt
) {
    public static ReservationResponse from(ReservationProcess process) {
        return new ReservationResponse(
                process.id(),
                process.professionalId(),
                process.appointmentDate(),
                process.startTime(),
                process.status().name(),
                process.holdId(),
                process.expiresAt());
    }
}