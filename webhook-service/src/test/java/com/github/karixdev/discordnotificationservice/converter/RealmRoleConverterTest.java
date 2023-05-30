package com.github.karixdev.discordnotificationservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RealmRoleConverterTest {
    RealmRoleConverter underTest = new RealmRoleConverter();

    @Test
    void GivenJwtWithoutRealmAccessClaim_WhenConvert_ThenReturnsEmptyCollection() {
        // Given
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS),
                Map.of(
                        "alg", "HS256",
                        "typ", "JWT"
                ),
                Map.of(
                        "subject", "xyz"
                )
        );

        // When
        Collection<GrantedAuthority> result = underTest.convert(jwt);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenJwtWithoutRealmAccessRoles_WhenConvert_ThenReturnsEmptyCollection() {
        // Given
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS),
                Map.of(
                        "alg", "HS256",
                        "typ", "JWT"
                ),
                Map.of("realm_access", Map.of("key", "value"))
        );

        // When
        Collection<GrantedAuthority> result = underTest.convert(jwt);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenValidJwt_WhenConvert_ThenReturnsCorrectCollection() {
        // Given
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS),
                Map.of(
                        "alg", "HS256",
                        "typ", "JWT"
                ),
                Map.of("realm_access", Map.of(
                        "roles", List.of("admin", "user"))
                )
        );

        // When
        Collection<GrantedAuthority> result = underTest.convert(jwt);

        // Then
        assertThat(result)
                .containsAll(Set.of(
                        new SimpleGrantedAuthority("ROLE_admin"),
                        new SimpleGrantedAuthority("ROLE_user")
                ));
    }
}