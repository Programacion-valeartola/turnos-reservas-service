package com.valeartola.turnos.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    record Response(int status, String body) {
    }

    @Value("${local.server.port}")
    private int port;

    private Response register(String json) {
        return RestClient.create("http://localhost:" + port).post().uri("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .exchange((request, response) -> new Response(
                        response.getStatusCode().value(),
                        new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)));
    }

    private static String json(String login, String password) {
        return """
                {"login":"%s","password":"%s","firstName":"Test","lastName":"Usuario",
                 "email":"%s@example.com","langKey":"es"}
                """.formatted(login, password, login);
    }

    /** Login único por ejecución, para no chocar con usuarios de pruebas anteriores. */
    private static String uniqueLogin() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void registroExitosoDevuelve201SinLaContraseña() {
        Response response = register(json(uniqueLogin(), "claveSecreta99"));

        assertThat(response.status()).isEqualTo(201);
        assertThat(response.body()).contains("ROLE_USER");
        assertThat(response.body()).doesNotContain("claveSecreta99", "password");
    }

    @Test
    void loginRepetidoDevuelve400ConCode() {
        String login = uniqueLogin();
        register(json(login, "clave1234"));

        Response response = register(json(login, "clave1234"));

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.body()).contains("LOGIN_ALREADY_USED");
    }

    @Test
    void contraseñaDemasiadoCortaDevuelve400SinMostrarElValor() {
        Response response = register(json(uniqueLogin(), "12"));

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.body()).contains("VALIDATION_ERROR", "password");
        assertThat(response.body()).doesNotContain("\"12\"");
    }
}