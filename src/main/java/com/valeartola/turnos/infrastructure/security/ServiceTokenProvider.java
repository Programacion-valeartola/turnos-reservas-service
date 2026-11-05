package com.valeartola.turnos.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Emite el JWT técnico con el que el servicio de turnos se identifica ante el catálogo
 * (decisión de la consulta 1: JWT de servicio, no se propaga el token del usuario).
 * - sub = "turnos-service", auth = "ROLE_SERVICE", firmado con la clave compartida.
 * - Vida corta (5 minutos por defecto); se reutiliza y se renueva automáticamente.
 * - Nunca sale del backend: no se envía a la aplicación KMP.
 */
@Component
public class ServiceTokenProvider {

    public static final String SERVICE_SUBJECT = "turnos-service";
    public static final String SERVICE_ROLE = "ROLE_SERVICE";

    /** Se renueva un poco antes de vencer, para no enviar un token a punto de expirar. */
    static final Duration RENEW_MARGIN = Duration.ofSeconds(30);

    private final JwtEncoder encoder;
    private final Duration validity;
    private final Clock clock;

    private String cachedToken;
    private Instant cachedExpiresAt = Instant.EPOCH;

    @Autowired
    public ServiceTokenProvider(@Value("${jwt.secret}") String base64Secret,
                                @Value("${jwt.service-token-validity-seconds:300}") long validitySeconds) {
        this(base64Secret, validitySeconds, Clock.systemUTC());
    }

    /** Constructor para pruebas, con un reloj controlable. */
    ServiceTokenProvider(String base64Secret, long validitySeconds, Clock clock) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(JwtSecretKeys.fromBase64(base64Secret)));
        this.validity = Duration.ofSeconds(validitySeconds);
        this.clock = clock;
    }

    /** Devuelve un token de servicio vigente, reutilizando el anterior si todavía sirve. */
    public synchronized String getToken() {
        Instant now = clock.instant();
        if (cachedToken == null || !now.isBefore(cachedExpiresAt.minus(RENEW_MARGIN))) {
            Instant expiresAt = now.plus(validity);
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer(JwtTokenIssuer.ISSUER)
                    .subject(SERVICE_SUBJECT)
                    .issuedAt(now)
                    .expiresAt(expiresAt)
                    .claim(JwtTokenIssuer.AUTHORITIES_CLAIM, SERVICE_ROLE)
                    .build();
            cachedToken = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                    .getTokenValue();
            cachedExpiresAt = expiresAt;
        }
        return cachedToken;
    }
}