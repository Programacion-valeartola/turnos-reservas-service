package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.ProfessionalDisabledException;
import com.valeartola.turnos.domain.model.AvailableSlot;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.domain.model.WeeklySchedule;
import com.valeartola.turnos.domain.ports.out.CatalogProvider;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailabilityUseCaseImplTest {

    private static final Long PROFESSIONAL_ID = 15L;
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 23);

    @Mock
    private CatalogProvider catalogProvider;

    @Mock
    private CatedraAppointmentsGateway catedra;

    @InjectMocks
    private GetAvailabilityUseCaseImpl useCase;

    private static WeeklySchedule schedule(DayOfWeek day, boolean enabled) {
        return new WeeklySchedule(day, LocalTime.of(18, 0), LocalTime.of(21, 0), 30, enabled);
    }

    private static ProfessionalAgenda agenda(boolean enabled, WeeklySchedule... schedules) {
        return new ProfessionalAgenda(PROFESSIONAL_ID, "Ana Gomez", enabled, "Clínica", List.of(schedules));
    }

    @Test
    void devuelveLosTurnosQueNoEstanOcupados() {
        when(catalogProvider.getProfessional(PROFESSIONAL_ID))
                .thenReturn(agenda(true, schedule(DayOfWeek.MONDAY, true)));
        when(catedra.findOccupancies(PROFESSIONAL_ID, MONDAY, MONDAY)).thenReturn(List.of(
                new Occupancy(MONDAY, LocalTime.of(19, 0), "CONFIRMED", null),
                new Occupancy(MONDAY, LocalTime.of(20, 30), "HELD", Instant.now().plusSeconds(300))));

        List<AvailableSlot> slots = useCase.getAvailability(PROFESSIONAL_ID, MONDAY);

        assertThat(slots).extracting(AvailableSlot::startTime).containsExactly(
                LocalTime.of(18, 0), LocalTime.of(18, 30), LocalTime.of(19, 30), LocalTime.of(20, 0));
        assertThat(slots.getFirst().endTime()).isEqualTo(LocalTime.of(18, 30));
    }

    @Test
    void unProfesionalDeshabilitadoNoOfreceTurnos() {
        when(catalogProvider.getProfessional(PROFESSIONAL_ID))
                .thenReturn(agenda(false, schedule(DayOfWeek.MONDAY, true)));

        assertThatThrownBy(() -> useCase.getAvailability(PROFESSIONAL_ID, MONDAY))
                .isInstanceOf(ProfessionalDisabledException.class);
        verifyNoInteractions(catedra);
    }

    @Test
    void siEseDiaNoAtiendeDevuelveVacioSinConsultarALaCatedra() {
        when(catalogProvider.getProfessional(PROFESSIONAL_ID))
                .thenReturn(agenda(true, schedule(DayOfWeek.TUESDAY, true)));

        assertThat(useCase.getAvailability(PROFESSIONAL_ID, MONDAY)).isEmpty();
        verifyNoInteractions(catedra);
    }

    @Test
    void ignoraLosHorariosDeshabilitados() {
        when(catalogProvider.getProfessional(PROFESSIONAL_ID))
                .thenReturn(agenda(true, schedule(DayOfWeek.MONDAY, false)));

        assertThat(useCase.getAvailability(PROFESSIONAL_ID, MONDAY)).isEmpty();
        verifyNoInteractions(catedra);
    }
}