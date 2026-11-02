package com.valeartola.turnos.domain.model;

import java.time.Instant;
import java.util.Set;

/**
 * Usuario final de la aplicación (modelo compatible con JHipster).
 * passwordHash es el hash BCrypt; nunca se guarda ni se devuelve la contraseña original.
 */
public record User(
        Long id,
        String login,
        String passwordHash,
        String firstName,
        String lastName,
        String email,
        String imageUrl,
        boolean activated,
        String langKey,
        Set<String> authorities,
        Instant createdDate
) {

    /** Se redefine para que el hash nunca aparezca en logs. */
    @Override
    public String toString() {
        return "User[id=" + id + ", login=" + login + ", authorities=" + authorities + "]";
    }
}