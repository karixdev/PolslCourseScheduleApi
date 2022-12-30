package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EmailVerificationTokenRepositoryTest {
    @Autowired
    EmailVerificationTokenRepository underTest;

    @Autowired
    TestEntityManager em;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("email@email.pl")
                .password("secret-password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(Boolean.FALSE)
                .build();

        em.persistAndFlush(user);
    }

    @Test
    void GivenNonExistingToken_WhenFindByToken_ThenReturnsEmptyOptional() {
        // Given
        String token = "i-do-not-exist";

        EmailVerificationToken eToken = EmailVerificationToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();

        em.persistAndFlush(eToken);

        // When
        var result = underTest.findByToken(token);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingToken_WhenFindByToken_ThenReturnsOptionalWithCorrectEmailVerificationToken() {
        // Given
        String token = "token";

        EmailVerificationToken eToken = EmailVerificationToken.builder()
                .user(user)
                .token("token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();

        em.persistAndFlush(eToken);

        // When
        var result = underTest.findByToken(token);

        // Then
        assertThat(result).isNotEmpty();

        var resultToken = result.get();

        assertThat(resultToken).isEqualTo(eToken);
    }
}
