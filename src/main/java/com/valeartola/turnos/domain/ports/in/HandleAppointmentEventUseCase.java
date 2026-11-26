package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.AppointmentEvent;

/** Puerto de entrada: procesar un evento de acciones de turnos recibido de la cátedra. */
public interface HandleAppointmentEventUseCase {

    void handle(AppointmentEvent event);
}