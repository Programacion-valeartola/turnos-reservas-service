package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.UserAlreadyExistsException;
import com.valeartola.turnos.domain.model.Authority;
import com.valeartola.turnos.domain.model.NewUserRegistration;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.in.RegisterUserUseCase;
import com.valeartola.turnos.domain.ports.out.PasswordHasher;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Registra un usuario final: activo de inmediato (sin verificación por email),
 * con ROLE_USER y la contraseña guardada como hash.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    static final String DEFAULT_LANG_KEY = "es";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public User register(NewUserRegistration registration) {
        // Como en JHipster, login y email se guardan en minúsculas
        String login = registration.login().trim().toLowerCase(Locale.ROOT);
        String email = registration.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByLogin(login)) {
            throw new UserAlreadyExistsException(UserAlreadyExistsException.LOGIN_ALREADY_USED,
                    "El login ya está registrado");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(UserAlreadyExistsException.EMAIL_ALREADY_USED,
                    "El email ya está registrado");
        }

        String langKey = registration.langKey() == null || registration.langKey().isBlank()
                ? DEFAULT_LANG_KEY : registration.langKey();

        User newUser = new User(
                null,
                login,
                passwordHasher.hash(registration.password()),
                registration.firstName(),
                registration.lastName(),
                email,
                registration.imageUrl(),
                true,
                langKey,
                Set.of(Authority.USER),
                null);

        User saved = userRepository.save(newUser);
        log.info("Usuario registrado: {}", saved.login());
        return saved;
    }
}