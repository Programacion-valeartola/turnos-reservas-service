package com.valeartola.turnos.infrastructure.web.dto;

import com.valeartola.turnos.domain.model.User;

import java.util.Set;

/** Datos públicos del usuario: nunca incluye la contraseña ni su hash. */
public record UserResponse(
        Long id,
        String login,
        String firstName,
        String lastName,
        String email,
        String imageUrl,
        boolean activated,
        String langKey,
        Set<String> authorities
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.login(), user.firstName(), user.lastName(), user.email(),
                user.imageUrl(), user.activated(), user.langKey(), user.authorities());
    }
}