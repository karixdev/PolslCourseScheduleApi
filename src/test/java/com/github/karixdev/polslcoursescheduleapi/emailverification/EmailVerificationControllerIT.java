package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRepository;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EmailVerificationControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserService userService;

    @Autowired
    EmailVerificationService emailVerificationService;

    @Autowired
    EmailVerificationTokenRepository tokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationProperties properties;

    @Autowired
    Clock clock;

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotVerifyNotExistingToken() {
        webClient.post().uri("/api/v1/email-verification/not-existing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotVerifyExpiredToken() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.FALSE
        );

        LocalDateTime now = LocalDateTime.now(clock);
        String token = "abc-efg";

        tokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .createdAt(now.minusHours(34))
                .expiresAt(now.minusHours(10))
                .build());

        webClient.post().uri("/api/v1/email-verification/" + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldNotVerifyAlreadyVerified() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.TRUE
        );

        LocalDateTime now = LocalDateTime.now(clock);
        String token = "abc-efg";

        tokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .createdAt(now.minusHours(34))
                .expiresAt(now.minusHours(10))
                .confirmedAt(now.minusHours(20))
                .build());

        webClient.post().uri("/api/v1/email-verification/" + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldVerifyEmail() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.FALSE
        );

        EmailVerificationToken token =
                emailVerificationService.createToken(user);

        webClient.post().uri("/api/v1/email-verification/" + token.getToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.message").isEqualTo("success");

        assertThat(tokenRepository.findAll().get(0).getConfirmedAt()).isNotNull();
        assertThat(userRepository.findAll().get(0).getIsEnabled()).isTrue();
    }

    @Test
    void shouldNotResendTokenForNotExistingUser() {
        String payload = """
                {
                    "email": "i-do-not-exist@email.com"
                }
                """;

        webClient.post().uri("/api/v1/email-verification/resend")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotResendTokenForAlreadyEnabledUser() {
        userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.TRUE
        );

        String payload = """
                {
                    "email": "email@email.com"
                }
                """;

        webClient.post().uri("/api/v1/email-verification/resend")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotResendTokenForUserWhoRequestedTooManyTokensInHour() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.FALSE
        );

        for (int i = 0; i < properties.getMaxNumberOfMailsPerHour(); i++) {
            LocalDateTime now = LocalDateTime.now(clock);

            tokenRepository.save(EmailVerificationToken.builder()
                    .user(user)
                    .token("token-" + i)
                    .createdAt(now.minusMinutes(10 + i))
                    .expiresAt(now.minusMinutes(10 + i).plusHours(24))
                    .build());
        }

        String payload = """
                {
                    "email": "email@email.com"
                }
                """;

        webClient.post().uri("/api/v1/email-verification/resend")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(tokenRepository.findAll())
                .hasSize(properties.getMaxNumberOfMailsPerHour());
    }
}
