package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.ReservationProcess;

import java.time.LocalDate;
import java.time.LocalTime;

/** Puerto de entrada: el usuario elige un turno y se solicita el hold a la cátedra. */
public interface StartReservationUseCase {

    ReservationProcess start(String login, Long professionalId, LocalDate date, LocalTime startTime);
}