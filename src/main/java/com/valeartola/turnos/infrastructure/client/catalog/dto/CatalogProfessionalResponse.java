package com.valeartola.turnos.infrastructure.client.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.domain.model.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** Respuesta de GET /api/professionals/{id} del servicio de catálogo. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProfessionalResponse(
        Long id,
        String fullName,
        boolean enabled,
        Category category,
        List<Schedule> schedules
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Category(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Schedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                           int slotDurationMinutes, boolean enabled) {
    }

    public ProfessionalAgenda toDomain() {
        return new ProfessionalAgenda(
                id,
                fullName,
                enabled,
                category == null ? null : category.name(),
                schedules == null ? List.of() : schedules.stream()
                        .map(s -> new WeeklySchedule(s.dayOfWeek(), s.startTime(), s.endTime(),
                                s.slotDurationMinutes(), s.enabled()))
                        .toList());
    }
}