package com.github.karixdev.polslcoursescheduleapi.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Getter
@Service
public class JwtProperties {
    private final String issuer;
    private final Long tokenExpirationHours;

    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JwtProperties(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.token-expiration-hours}") Long tokenExpirationHours,
            @Value("${jwt.key.public}") RSAPublicKey publicKey,
            @Value("${jwt.key.private}") RSAPrivateKey privateKey
    ) {
        this.issuer = issuer;
        this.tokenExpirationHours = tokenExpirationHours;

        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        this.jwtVerifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }
}
