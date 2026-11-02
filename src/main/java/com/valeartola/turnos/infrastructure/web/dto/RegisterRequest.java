package com.valeartola.turnos.infrastructure.web.dto;

import com.valeartola.turnos.domain.model.NewUserRegistration;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/register. Las reglas siguen al modelo de usuario de JHipster.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = LOGIN_REGEX) String login,
        @NotBlank @Size(min = 4, max = 100) String password,
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @NotBlank @Email @Size(min = 5, max = 191) String email,
        @Size(max = 256) String imageUrl,
        @Size(min = 2, max = 10) String langKey
) {

    /** Expresión de login de JHipster: letras, números y algunos símbolos, o un email. */
    public static final String LOGIN_REGEX =
            "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public NewUserRegistration toDomain() {
        return new NewUserRegistration(login, password, firstName, lastName, email, imageUrl, langKey);
    }

    /** La contraseña nunca aparece en logs. */
    @Override
    public String toString() {
        return "RegisterRequest[login=" + login + ", email=" + email + "]";
    }
}