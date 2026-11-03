package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.AccessToken;

/** Puerto de entrada: iniciar sesión con usuario y contraseña. */
public interface AuthenticateUserUseCase {

    /** @throws com.valeartola.turnos.domain.exception.InvalidCredentialsException si no coinciden */
    AccessToken authenticate(String username, String password);
}