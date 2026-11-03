package com.valeartola.turnos.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/authenticate (mismos nombres que JHipster). */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {

    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}