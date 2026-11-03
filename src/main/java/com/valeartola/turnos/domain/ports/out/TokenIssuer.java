package com.valeartola.turnos.domain.ports.out;

import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.model.User;

/** Puerto de salida: emite el token de acceso de un usuario autenticado. */
public interface TokenIssuer {

    AccessToken issueFor(User user);
}