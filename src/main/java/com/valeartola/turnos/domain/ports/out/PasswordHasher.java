package com.valeartola.turnos.domain.ports.out;

/**
 * Puerto de salida: convierte contraseñas en hash.
 * El dominio no sabe que detrás está BCrypt de Spring Security.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    /** Compara una contraseña escrita por el usuario con el hash guardado. */
    boolean matches(String rawPassword, String passwordHash);
}