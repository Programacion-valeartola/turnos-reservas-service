package com.valeartola.turnos.domain.ports.in;

import com.valeartola.turnos.domain.model.NewUserRegistration;
import com.valeartola.turnos.domain.model.User;

/** Puerto de entrada: registrar un usuario final. */
public interface RegisterUserUseCase {

    User register(NewUserRegistration registration);
}