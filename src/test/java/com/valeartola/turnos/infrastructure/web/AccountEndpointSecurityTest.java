package com.valeartola.turnos.infrastructure.web;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.valeartola.turnos.domain.model.Authority;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.infrastructure.security.JwtSecretKeys;
import com.valeartola.turnos.infrastructure.security.JwtTokenIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de seguridad con el servicio levantado en un puerto al azar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountEndpointSecurityTest {

    record Response(int status, String body) {
    }

    @Value("${local.server.port}")
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    private Response getAccount(String token) {
        var request = client().get().uri("/api/account");
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.exchange((req, res) -> new Response(
                res.getStatusCode().value(),
                new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8)));
    }

    /** Registra un usuario nuevo, inicia sesión y devuelve [login, token]. */
    private String[] registrarEIniciarSesion() {
        String login = "test-" + UUID.randomUUID().toString().substring(0, 8);
        client().post().uri("/api/register").contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"login":"%s","password":"clave1234","email":"%s@example.com"}
                        """.formatted(login, login))
                .retrieve().toBodilessEntity();
        String body = client().post().uri("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"username":"%s","password":"clave1234"}
                        """.formatted(login))
                .retrieve().body(String.class);
        String token = body.replaceAll(".*\"id_token\"\\s*:\\s*\"([^\"]+)\".*", "$1").trim();
        return new String[]{login, token};
    }

    @Test
    void conTokenValidoDevuelveLosDatosDelUsuarioDelToken() {
        String[] sesion = registrarEIniciarSesion();

        Response response = getAccount(sesion[1]);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).contains("\"login\":\"" + sesion[0] + "\"", "ROLE_USER");
        assertThat(response.body()).doesNotContain("password");
    }

    @Test
    void sinTokenResponde401() {
        assertThat(getAccount(null).status()).isEqualTo(401);
    }

    @Test
    void conTokenAdulteradoResponde401() {
        String token = registrarEIniciarSesion()[1];
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThat(getAccount(adulterado).status()).isEqualTo(401);
    }

    @Test
    void conTokenVencidoResponde401() {
        // Token emitido hace 2 horas, que venció hace 1 hora
        Instant emitido = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("alguien")
                .issuedAt(emitido)
                .expiresAt(emitido.plus(Duration.ofHours(1)))
                .claim(JwtTokenIssuer.AUTHORITIES_CLAIM, Authority.USER)
                .build();
        String vencido = new NimbusJwtEncoder(new ImmutableSecret<>(JwtSecretKeys.fromBase64(jwtSecret)))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThat(getAccount(vencido).status()).isEqualTo(401);
    }

    @Test
    void unTokenDeServicioNoPuedeVerUnaCuenta() {
        User servicio = new User(1L, "turnos-service", "hash", null, null, null,
                null, true, null, Set.of(Authority.SERVICE), Instant.now());
        String tokenServicio = new JwtTokenIssuer(jwtSecret, 3600).issueFor(servicio).value();

        assertThat(getAccount(tokenServicio).status()).isEqualTo(403);
    }
}