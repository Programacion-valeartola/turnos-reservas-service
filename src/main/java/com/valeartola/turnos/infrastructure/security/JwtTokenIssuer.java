package com.valeartola.turnos.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.TokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Adaptador de salida: emite JWT firmados con HS256 y la clave compartida JWT_SECRET.
 * Formato compatible con JHipster: los roles van en el claim "auth" separados por espacios.
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    public static final String AUTHORITIES_CLAIM = "auth";
    public static final String USER_ID_CLAIM = "userId";
    public static final String ISSUER = "turnos-service";

    private final JwtEncoder encoder;
    private final Duration validity;

    public JwtTokenIssuer(@Value("${jwt.secret}") String base64Secret,
                          @Value("${jwt.token-validity-seconds:86400}") long validitySeconds) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(JwtSecretKeys.fromBase64(base64Secret)));
        this.validity = Duration.ofSeconds(validitySeconds);
    }

    @Override
    public AccessToken issueFor(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(validity);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.login())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(AUTHORITIES_CLAIM, String.join(" ", user.authorities().stream().sorted().toList()))
                .claim(USER_ID_CLAIM, user.id())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, expiresAt);
    }
}