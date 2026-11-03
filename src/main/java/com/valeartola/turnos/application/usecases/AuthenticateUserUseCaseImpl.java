package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.InvalidCredentialsException;
import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.in.AuthenticateUserUseCase;
import com.valeartola.turnos.domain.ports.out.PasswordHasher;
import com.valeartola.turnos.domain.ports.out.TokenIssuer;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Inicia sesión: busca el usuario, compara la contraseña con el hash y emite el token.
 * Ante cualquier falla (usuario inexistente, desactivado o contraseña incorrecta)
 * responde con el mismo error, sin revelar qué dato falló.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Override
    public AccessToken authenticate(String username, String password) {
        String login = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);

        Optional<User> user = userRepository.findByLogin(login).filter(User::activated);
        if (user.isEmpty() || password == null || !passwordHasher.matches(password, user.get().passwordHash())) {
            log.info("Inicio de sesión fallido para '{}'", login);   // nunca se registra la contraseña
            throw new InvalidCredentialsException();
        }

        log.info("Inicio de sesión exitoso: {}", login);
        return tokenIssuer.issueFor(user.get());
    }
}