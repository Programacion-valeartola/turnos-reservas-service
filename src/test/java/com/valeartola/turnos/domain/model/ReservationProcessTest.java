package com.valeartola.turnos.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationProcessTest {

    private final ReservationProcess created =
            ReservationProcess.start(1L, 15L, LocalDate.of(2026, 11, 23), LocalTime.of(18, 30));

    @Test
    void unProcesoNuevoArrancaEnCreated() {
        assertThat(created.status()).isEqualTo(ReservationStatus.CREATED);
        assertThat(created.holdId()).isNull();
    }

    @Test
    void conElHoldPasaAHeldYGuardaLosIdsDeLaCatedra() {
        Instant expiresAt = Instant.parse("2026-11-23T21:40:00Z");
        ReservationProcess held = created.withHold(new Hold("hold-1", "process-1", "HELD", expiresAt));

        assertThat(held.status()).isEqualTo(ReservationStatus.HELD);
        assertThat(held.holdId()).isEqualTo("hold-1");
        assertThat(held.reservationProcessId()).isEqualTo("process-1");
        assertThat(held.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void siLaCatedraRechazaPasaAFailedConElMotivo() {
        ReservationProcess failed = created.failed("SLOT_ALREADY_RESERVED");

        assertThat(failed.status()).isEqualTo(ReservationStatus.FAILED);
        assertThat(failed.failureReason()).isEqualTo("SLOT_ALREADY_RESERVED");
        assertThat(failed.status().isFinal()).isTrue();
    }

    @Test
    void noSePuedeRetenerDosVeces() {
        ReservationProcess held = created.withHold(new Hold("hold-1", "process-1", "HELD", Instant.now()));

        assertThatThrownBy(() -> held.withHold(new Hold("hold-2", "process-2", "HELD", Instant.now())))
                .isInstanceOf(IllegalStateException.class);
    }
}