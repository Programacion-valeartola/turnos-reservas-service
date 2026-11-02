package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.UserAlreadyExistsException;
import com.valeartola.turnos.domain.model.Authority;
import com.valeartola.turnos.domain.model.NewUserRegistration;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserUseCaseImplTest {

    /** Base falsa en memoria. */
    static class FakeUserRepository implements UserRepository {
        final List<User> users = new ArrayList<>();

        @Override
        public boolean existsByLogin(String login) {
            return users.stream().anyMatch(u -> u.login().equals(login));
        }

        @Override
        public boolean existsByEmail(String email) {
            return users.stream().anyMatch(u -> u.email().equalsIgnoreCase(email));
        }

        @Override
        public User save(User user) {
            User saved = new User((long) users.size() + 1, user.login(), user.passwordHash(), user.firstName(),
                    user.lastName(), user.email(), user.imageUrl(), user.activated(), user.langKey(),
                    user.authorities(), user.createdDate());
            users.add(saved);
            return saved;
        }
    }

    private final FakeUserRepository repository = new FakeUserRepository();
    // Hash falso y predecible para las pruebas
    private final RegisterUserUseCaseImpl useCase =
            new RegisterUserUseCaseImpl(repository, raw -> "hash(" + raw + ")");

    private static NewUserRegistration registration(String login, String email) {
        return new NewUserRegistration(login, "clave1234", "Ana", "Gómez", email, null, null);
    }

    @Test
    void registraUnUsuarioActivoConRolUserYContraseñaHasheada() {
        User user = useCase.register(registration("ana", "ana@example.com"));

        assertThat(user.id()).isNotNull();
        assertThat(user.activated()).isTrue();
        assertThat(user.authorities()).containsExactly(Authority.USER);
        assertThat(user.passwordHash()).isEqualTo("hash(clave1234)");
        assertThat(user.langKey()).isEqualTo("es");
    }

    @Test
    void guardaLoginYEmailEnMinusculas() {
        User user = useCase.register(registration("  Ana.Gomez ", "Ana@Example.COM"));

        assertThat(user.login()).isEqualTo("ana.gomez");
        assertThat(user.email()).isEqualTo("ana@example.com");
    }

    @Test
    void rechazaUnLoginRepetidoSinImportarMayusculas() {
        useCase.register(registration("ana", "ana@example.com"));

        assertThatThrownBy(() -> useCase.register(registration("ANA", "otra@example.com")))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("code").isEqualTo(UserAlreadyExistsException.LOGIN_ALREADY_USED);
        assertThat(repository.users).hasSize(1);
    }

    @Test
    void rechazaUnEmailRepetido() {
        useCase.register(registration("ana", "ana@example.com"));

        assertThatThrownBy(() -> useCase.register(registration("otra", "ANA@example.com")))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("code").isEqualTo(UserAlreadyExistsException.EMAIL_ALREADY_USED);
    }

    @Test
    void laContraseñaNoApareceAlConvertirATexto() {
        NewUserRegistration registration = registration("ana", "ana@example.com");
        User user = useCase.register(registration);

        assertThat(registration.toString()).doesNotContain("clave1234");
        assertThat(user.toString()).doesNotContain("hash(");
    }
}