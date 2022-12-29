package com.github.karixdev.polslcoursescheduleapi.auth;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationToken;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationTokenRepository;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRepository;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.mail.internet.MimeMessage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuthControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationTokenRepository tokenRepository;

    @Autowired
    UserService userService;

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP)
                    .withConfiguration(GreenMailConfiguration.aConfig()
                            .withUser("greenmail-user", "greenmail-password"))
                    .withPerMethodLifecycle(false);

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotRegisterUserWhenProvidedNotAvailableEmail() {
        userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_USER,
                Boolean.FALSE
        );

        String payload = """
                {
                    "email": "email@email.com",
                    "password": "password"
                }
                """;

        webClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        assertThat(userRepository.findAll().size())
                .isEqualTo(1);
    }

    @Test
    void shouldRegisterUser() {
        String payload = """
                {
                    "email": "email@email.com",
                    "password": "password"
                }
                """;

        webClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("success");

        assertThat(userRepository.findAll()).isNotEmpty();

        User user = userRepository.findAll().get(0);

        assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(user.getEmail()).isEqualTo("email@email.com");
        assertThat(user.getIsEnabled()).isEqualTo(Boolean.FALSE);

        assertThat(tokenRepository.findAll().size())
                .isEqualTo(1);

        EmailVerificationToken token = tokenRepository.findAll().get(0);

        assertThat(token.getUser()).isEqualTo(user);

        await().atMost(2, SECONDS).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

            assertThat(receivedMessages).hasSize(1);

            MimeMessage receivedMessage = receivedMessages[0];

            assertThat(receivedMessage.getSubject()).isEqualTo("Verify your email");
            assertThat(receivedMessage.getFrom()).hasSize(1);

            String from = receivedMessage.getFrom()[0].toString();
            assertThat(from).isEqualTo("test@polsl-course-schedule-api.com");

            assertThat(receivedMessage.getAllRecipients()).hasSize(1);

            String recipient = receivedMessage.getAllRecipients()[0].toString();
            assertThat(recipient).isEqualTo("email@email.com");
        });
    }
}
