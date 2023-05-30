package com.github.karixdev.discordnotificationservice.service;

import com.github.karixdev.discordnotificationservice.converter.RealmRoleConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    @InjectMocks
    SecurityService underTest;

    @Mock
    RealmRoleConverter realmRoleConverter;

    @Test
    void GivenJwtSuchThatRealmRoleConverterReturnsNull_WhenIsAdmin_ThenReturnsFalse() {
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

        when(realmRoleConverter.convert(eq(jwt)))
                .thenReturn(null);

        // When
        boolean result = underTest.isAdmin(jwt);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenJwtContainingAdminRole_WhenIsAdmin_ThenReturnsTrue() {
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

        when(realmRoleConverter.convert(eq(jwt)))
                .thenReturn(Set.of(
                        new SimpleGrantedAuthority("ROLE_user"),
                        new SimpleGrantedAuthority("ROLE_admin")
                ));

        // When
        boolean result = underTest.isAdmin(jwt);

        // Then
        assertThat(result).isTrue();
    }
}