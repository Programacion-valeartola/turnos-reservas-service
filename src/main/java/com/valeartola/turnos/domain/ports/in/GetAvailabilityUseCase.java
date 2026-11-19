package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.AvailableSlot;

import java.time.LocalDate;
import java.util.List;

/** Puerto de entrada: turnos libres de un profesional en una fecha. */
public interface GetAvailabilityUseCase {

    List<AvailableSlot> getAvailability(Long professionalId, LocalDate date);
}