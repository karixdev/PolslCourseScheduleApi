package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
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
public class EmailVerificationTokenRepositoryTest extends ContainersEnvironment {
    @Autowired
    EmailVerificationTokenRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenNonExistingToken_WhenFindByToken_ThenReturnsEmptyOptional() {
        // Given
        String token = "i-do-not-exist";

        User user = User.builder()
                .email("email-15@email.pl")
                .password("secret-password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(Boolean.FALSE)
                .build();

        em.persist(user);

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

        User user = User.builder()
                .email("email-12@email.pl")
                .password("secret-password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(Boolean.FALSE)
                .build();

        em.persist(user);

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
