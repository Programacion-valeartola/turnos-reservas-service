package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.ReservationProcess;

/** Puerto de entrada: confirmación inicial del hold de un proceso propio. */
public interface ConfirmHoldUseCase {

    ReservationProcess confirm(String login, Long processId);
}