package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.exception.ProfessionalDisabledException;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.model.ReservationStatus;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.CatalogProvider;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StartReservationUseCaseImplTest {

    private static final Long PROFESSIONAL_ID = 15L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 23);
    private static final LocalTime TIME = LocalTime.of(18, 30);
    private static final User USER = new User(7L, "mechi", "hash", "Mechi", "Artola",
            "mechi@example.com", null, true, "es", Set.of("ROLE_USER"), Instant.now());

    @Mock private UserRepository userRepository;
    @Mock private CatalogProvider catalogProvider;
    @Mock private CatedraAppointmentsGateway catedra;
    @Mock private ReservationProcessRepository processes;

    @InjectMocks
    private StartReservationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        when(userRepository.findByLogin("mechi")).thenReturn(Optional.of(USER));
        when(catalogProvider.getProfessional(PROFESSIONAL_ID)).thenReturn(agenda(true));
        // La "base" asigna el id 100 al guardar un proceso nuevo
        when(processes.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0)));
    }

    @Test
    void conHoldExitosoElProcesoQuedaEnHeld() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(catedra.createHold(PROFESSIONAL_ID, DATE, TIME, "user-7"))
                .thenReturn(new Hold("hold-1", "process-1", "HELD", expiresAt));

        ReservationProcess result = useCase.start("mechi", PROFESSIONAL_ID, DATE, TIME);

        assertThat(result.status()).isEqualTo(ReservationStatus.HELD);
        assertThat(result.userId()).isEqualTo(7L);
        assertThat(result.holdId()).isEqualTo("hold-1");
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void siElTurnoYaEstaReservadoElProcesoQuedaEnFailedYSeInformaElError() {
        when(catedra.createHold(any(), any(), any(), any()))
                .thenThrow(new CatedraException(CatedraError.SLOT_ALREADY_RESERVED, 409));

        assertThatThrownBy(() -> useCase.start("mechi", PROFESSIONAL_ID, DATE, TIME))
                .isInstanceOf(CatedraException.class);

        ArgumentCaptor<ReservationProcess> saved = ArgumentCaptor.forClass(ReservationProcess.class);
        verify(processes, times(2)).save(saved.capture());
        List<ReservationProcess> values = saved.getAllValues();
        assertThat(values.get(1).status()).isEqualTo(ReservationStatus.FAILED);
        assertThat(values.get(1).failureReason()).isEqualTo("SLOT_ALREADY_RESERVED");
    }

    @Test
    void siLaCatedraNoRespondeElProcesoQuedaEnCreated() {
        when(catedra.createHold(any(), any(), any(), any()))
                .thenThrow(new CatedraException(CatedraError.UNAVAILABLE, 503));

        assertThatThrownBy(() -> useCase.start("mechi", PROFESSIONAL_ID, DATE, TIME))
                .isInstanceOf(CatedraException.class);

        // Solo se guardó una vez (en CREATED): no se marca como fallido
        verify(processes, times(1)).save(any());
    }

    @Test
    void unProfesionalDeshabilitadoNoIniciaNingunProceso() {
        when(catalogProvider.getProfessional(PROFESSIONAL_ID)).thenReturn(agenda(false));

        assertThatThrownBy(() -> useCase.start("mechi", PROFESSIONAL_ID, DATE, TIME))
                .isInstanceOf(ProfessionalDisabledException.class);
        verify(processes, never()).save(any());
        verifyNoInteractions(catedra);
    }

    private static ProfessionalAgenda agenda(boolean enabled) {
        return new ProfessionalAgenda(PROFESSIONAL_ID, "Ana Gomez", enabled, "Clínica", List.of());
    }

    private static ReservationProcess withId(ReservationProcess p) {
        if (p.id() != null) {
            return p;
        }
        return new ReservationProcess(100L, p.userId(), p.professionalId(), p.appointmentDate(), p.startTime(),
                p.status(), p.holdId(), p.reservationProcessId(), p.requestEventId(), p.reservationId(),
                p.expiresAt(), p.failureReason());
    }
}