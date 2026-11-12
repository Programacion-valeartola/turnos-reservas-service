package com.valeartola.turnos.domain.model;

import com.valeartola.turnos.domain.exception.ProfessionalDisabledException;

import java.util.List;

/**
 * Información vigente de un profesional, consultada al servicio de catálogo en el momento.
 * Turnos no guarda una copia: la pide cada vez que inicia una operación.
 */
public record ProfessionalAgenda(
        Long professionalId,
        String fullName,
        boolean enabled,
        String categoryName,
        List<WeeklySchedule> schedules
) {

    /** Solo los horarios habilitados: son los que generan turnos. */
    public List<WeeklySchedule> activeSchedules() {
        return schedules.stream().filter(WeeklySchedule::enabled).toList();
    }

    /** Un profesional deshabilitado no admite nuevas reservas. */
    public void ensureBookable() {
        if (!enabled) {
            throw new ProfessionalDisabledException(professionalId);
        }
    }
}