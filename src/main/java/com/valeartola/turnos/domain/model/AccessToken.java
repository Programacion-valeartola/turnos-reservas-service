package com.valeartola.turnos.domain.model;

import java.time.Instant;

/** Token de acceso emitido al iniciar sesión, con su vencimiento. */
public record AccessToken(String value, Instant expiresAt) {

    /** El token es una credencial: nunca se muestra completo en logs. */
    @Override
    public String toString() {
        return "AccessToken[expiresAt=" + expiresAt + "]";
    }
}