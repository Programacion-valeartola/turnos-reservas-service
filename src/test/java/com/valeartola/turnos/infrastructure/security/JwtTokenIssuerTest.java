package com.valeartola.turnos.infrastructure.security;

import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.model.Authority;
import com.valeartola.turnos.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenIssuerTest {

    // Claves solo para pruebas (32 bytes), nunca la real
    private static final String SECRET = Base64.getEncoder().encodeToString("clave-de-prueba-de-32-bytes-ok!!".getBytes());
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString("otra-clave-de-prueba-de-32-bytes".getBytes());

    private final User user = new User(7L, "ana", "hash", "Ana", "Gómez", "ana@example.com",
            null, true, "es", Set.of(Authority.USER), Instant.now());

    private static NimbusJwtDecoder decoderFor(String secret) {
        return NimbusJwtDecoder.withSecretKey(JwtSecretKeys.fromBase64(secret)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    void elTokenSeValidaConLaClaveCompartidaYTraeUsuarioYRoles() {
        AccessToken token = new JwtTokenIssuer(SECRET, 86400).issueFor(user);

        Jwt jwt = decoderFor(SECRET).decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo("ana");
        assertThat(jwt.getClaimAsString(JwtTokenIssuer.AUTHORITIES_CLAIM)).isEqualTo("ROLE_USER");
        assertThat(jwt.getClaim(JwtTokenIssuer.USER_ID_CLAIM).toString()).isEqualTo("7");
    }

    @Test
    void elTokenVenceSegunLaDuracionConfigurada() {
        AccessToken token = new JwtTokenIssuer(SECRET, 86400).issueFor(user);

        assertThat(Duration.between(Instant.now(), token.expiresAt()))
                .isBetween(Duration.ofHours(23), Duration.ofHours(24));
    }

    @Test
    void unaClaveDistintaNoPuedeValidarElToken() {
        AccessToken token = new JwtTokenIssuer(SECRET, 86400).issueFor(user);

        assertThatThrownBy(() -> decoderFor(OTHER_SECRET).decode(token.value()))
                .isInstanceOf(JwtException.class);
    }
}