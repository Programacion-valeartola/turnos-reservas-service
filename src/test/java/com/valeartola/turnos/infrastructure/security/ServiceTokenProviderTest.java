package com.valeartola.turnos.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenProviderTest {

    // Clave solo para pruebas (32 bytes), nunca la real
    private static final String SECRET =
            Base64.getEncoder().encodeToString("clave-de-prueba-de-32-bytes-ok!!".getBytes());

    /** Reloj que se puede adelantar a mano. */
    static class MovableClock extends Clock {
        Instant now = Instant.now();

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }

        void advance(Duration duration) { now = now.plus(duration); }
    }

    /** Valida igual que el catálogo: HS256 con la misma clave. */
    private static Jwt decode(String token) {
        return NimbusJwtDecoder.withSecretKey(JwtSecretKeys.fromBase64(SECRET))
                .macAlgorithm(MacAlgorithm.HS256).build().decode(token);
    }

    @Test
    void elTokenIdentificaAlServicioConRolServiceYVidaCorta() {
        ServiceTokenProvider provider = new ServiceTokenProvider(SECRET, 300);

        Jwt jwt = decode(provider.getToken());

        assertThat(jwt.getSubject()).isEqualTo("turnos-service");
        assertThat(jwt.getClaimAsString("auth")).isEqualTo("ROLE_SERVICE");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void mientrasEstaVigenteReutilizaElMismoToken() {
        MovableClock clock = new MovableClock();
        ServiceTokenProvider provider = new ServiceTokenProvider(SECRET, 300, clock);

        String first = provider.getToken();
        clock.advance(Duration.ofMinutes(2));

        assertThat(provider.getToken()).isEqualTo(first);
    }

    @Test
    void cercaDelVencimientoEmiteUnTokenNuevo() {
        MovableClock clock = new MovableClock();
        ServiceTokenProvider provider = new ServiceTokenProvider(SECRET, 300, clock);

        String first = provider.getToken();
        clock.advance(Duration.ofSeconds(280));   // faltan 20 s: menos que el margen de 30 s

        assertThat(provider.getToken()).isNotEqualTo(first);
    }
}
