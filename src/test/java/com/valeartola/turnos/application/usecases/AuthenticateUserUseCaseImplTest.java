package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.InvalidCredentialsException;
import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.model.Authority;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.PasswordHasher;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticateUserUseCaseImplTest {

    private static User user(String login, boolean activated) {
        return new User(1L, login, "hash(clave1234)", "Ana", "Gómez", login + "@example.com",
                null, activated, "es", Set.of(Authority.USER), Instant.now());
    }

    /** Base falsa con un único usuario. */
    private User storedUser = user("ana", true);

    private final UserRepository repository = new UserRepository() {
        @Override public boolean existsByLogin(String login) { return false; }
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public User save(User user) { return user; }
        @Override public Optional<User> findByLogin(String login) {
            return Optional.ofNullable(storedUser).filter(u -> u.login().equals(login));
        }
    };

    private final PasswordHasher hasher = new PasswordHasher() {
        @Override public String hash(String raw) { return "hash(" + raw + ")"; }
        @Override public boolean matches(String raw, String hash) { return hash.equals("hash(" + raw + ")"); }
    };

    private final AuthenticateUserUseCaseImpl useCase = new AuthenticateUserUseCaseImpl(
            repository, hasher, u -> new AccessToken("token-de-" + u.login(), Instant.now().plusSeconds(3600)));

    @Test
    void conCredencialesCorrectasDevuelveUnToken() {
        AccessToken token = useCase.authenticate("ana", "clave1234");

        assertThat(token.value()).isEqualTo("token-de-ana");
    }

    @Test
    void elLoginNoDistingueMayusculas() {
        assertThat(useCase.authenticate("  ANA ", "clave1234").value()).isEqualTo("token-de-ana");
    }

    @Test
    void conContraseñaIncorrectaFalla() {
        assertThatThrownBy(() -> useCase.authenticate("ana", "otra"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void unUsuarioInexistenteRecibeElMismoError() {
        assertThatThrownBy(() -> useCase.authenticate("nadie", "clave1234"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Usuario o contraseña incorrectos");
    }

    @Test
    void unUsuarioDesactivadoNoPuedeIniciarSesion() {
        storedUser = user("ana", false);

        assertThatThrownBy(() -> useCase.authenticate("ana", "clave1234"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}