package com.github.karixdev.polslcoursescheduleapi.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.karixdev.polslcoursescheduleapi.jwt.exception.InvalidJwtException;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService {
    private final JwtProperties properties;
    private final Clock clock;

    public String createToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(
                properties.getTokenExpirationHours(),
                ChronoUnit.HOURS
        );

        return JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(userPrincipal.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(properties.getAlgorithm());
    }

    private Optional<DecodedJWT> decodeToken(String token) {
        try {
            return Optional.of(properties.getJwtVerifier().verify(token));
        } catch (JWTVerificationException e) {
            log.error("invalid token", e);
        }

        return Optional.empty();
    }

    public boolean isTokenValid(String token) {
        return decodeToken(token).isPresent();
    }

    public String getEmailFromToken(String token) {
        Optional<DecodedJWT> optionalToken = decodeToken(token);

        if (optionalToken.isEmpty()) {
            throw new InvalidJwtException();
        }

        return optionalToken.get().getSubject();
    }
}
