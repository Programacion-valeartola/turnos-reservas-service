package com.valeartola.turnos.domain.model;

/**
 * Datos que envía una persona para registrarse. Contiene la contraseña en texto plano,
 * que solo vive en memoria hasta convertirse en hash.
 */
public record NewUserRegistration(
        String login,
        String password,
        String firstName,
        String lastName,
        String email,
        String imageUrl,
        String langKey
) {

    /** Se redefine para que la contraseña nunca aparezca en logs. */
    @Override
    public String toString() {
        return "NewUserRegistration[login=" + login + ", email=" + email + "]";
    }
}