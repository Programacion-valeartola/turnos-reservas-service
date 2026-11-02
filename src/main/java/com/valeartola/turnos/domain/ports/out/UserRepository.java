package com.valeartola.turnos.domain.ports.out;

import com.valeartola.turnos.domain.model.User;

/** Puerto de salida: donde se guardan los usuarios finales. */
public interface UserRepository {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    User save(User user);
}