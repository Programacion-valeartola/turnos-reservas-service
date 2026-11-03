package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.User;

/** Puerto de entrada: datos del usuario autenticado. */
public interface GetCurrentAccountUseCase {

    /** @param login el login tomado del token (claim "sub"), nunca de datos enviados por el cliente */
    User getAccount(String login);
}