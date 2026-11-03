package com.valeartola.turnos.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta del login con el mismo formato que JHipster: {"id_token": "..."}. */
public record TokenResponse(@JsonProperty("id_token") String idToken) {
}