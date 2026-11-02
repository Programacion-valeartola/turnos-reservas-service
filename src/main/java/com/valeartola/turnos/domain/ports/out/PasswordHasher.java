package com.valeartola.turnos.domain.ports.out;

/**
 * Puerto de salida: convierte contraseñas en hash.
 * El dominio no sabe que detrás está BCrypt de Spring Security.
 */
public interface PasswordHasher {

    String hash(String rawPassword);
}