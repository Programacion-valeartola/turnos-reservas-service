package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.model.AvailableSlot;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.domain.model.WeeklySchedule;
import com.valeartola.turnos.domain.ports.in.GetAvailabilityUseCase;
import com.valeartola.turnos.domain.ports.out.CatalogProvider;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Disponibilidad = turnos generados por los horarios vigentes del catálogo
 * menos las ocupaciones (CONFIRMED y HELD) informadas por la cátedra.
 */
@Component
@RequiredArgsConstructor
public class GetAvailabilityUseCaseImpl implements GetAvailabilityUseCase {

    private final CatalogProvider catalogProvider;
    private final CatedraAppointmentsGateway catedra;

    @Override
    public List<AvailableSlot> getAvailability(Long professionalId, LocalDate date) {
        // 1. Información vigente del profesional (si está deshabilitado, corta acá)
        ProfessionalAgenda agenda = catalogProvider.getProfessional(professionalId);
        agenda.ensureBookable();

        // 2. Solo los horarios habilitados de ese día de la semana
        List<WeeklySchedule> schedulesOfDay = agenda.activeSchedules().stream()
                .filter(schedule -> schedule.dayOfWeek() == date.getDayOfWeek())
                .toList();
        if (schedulesOfDay.isEmpty()) {
            return List.of();   // ese día no atiende: no hace falta consultar a la cátedra
        }

        // 3. Horarios ocupados según la cátedra
        Set<LocalTime> occupied = catedra.findOccupancies(professionalId, date, date).stream()
                .filter(occupancy -> date.equals(occupancy.date()))
                .map(Occupancy::startTime)
                .collect(Collectors.toSet());

        // 4. Turnos posibles que no están ocupados, ordenados por hora
        return schedulesOfDay.stream()
                .flatMap(schedule -> schedule.slotStartTimes().stream()
                        .map(start -> new AvailableSlot(date, start, start.plusMinutes(schedule.slotDurationMinutes()))))
                .filter(slot -> !occupied.contains(slot.startTime()))
                .distinct()
                .sorted(Comparator.comparing(AvailableSlot::startTime))
                .toList();
    }
}