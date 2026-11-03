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
class AuthenticationControllerTest {

    record Response(int status, String body) {
    }

    @Value("${local.server.port}")
    private int port;

    private Response post(String path, String json) {
        return RestClient.create("http://localhost:" + port).post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .exchange((request, response) -> new Response(
                        response.getStatusCode().value(),
                        new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)));
    }

    private String registrarUsuario(String password) {
        String login = "test-" + UUID.randomUUID().toString().substring(0, 8);
        post("/api/register", """
                {"login":"%s","password":"%s","email":"%s@example.com","langKey":"es"}
                """.formatted(login, password, login));
        return login;
    }

    @Test
    void conCredencialesCorrectasDevuelveElToken() {
        String login = registrarUsuario("clave1234");

        Response response = post("/api/authenticate", """
                {"username":"%s","password":"clave1234"}
                """.formatted(login));

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).contains("\"id_token\":\"eyJ");
    }

    @Test
    void conContraseñaIncorrectaDevuelve401SinDetalles() {
        String login = registrarUsuario("clave1234");

        Response response = post("/api/authenticate", """
                {"username":"%s","password":"incorrecta"}
                """.formatted(login));

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.body()).contains("INVALID_CREDENTIALS").doesNotContain("incorrecta\"");
    }

    @Test
    void unUsuarioInexistenteDevuelveElMismo401() {
        Response response = post("/api/authenticate", """
                {"username":"no-existe-nunca","password":"clave1234"}
                """);

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.body()).contains("INVALID_CREDENTIALS");
    }
}