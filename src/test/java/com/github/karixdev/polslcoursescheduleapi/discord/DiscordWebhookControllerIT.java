package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.jwt.JwtService;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRepository;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 8888)
public class DiscordWebhookControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserService userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    DiscordWebhookRepository discordWebhookRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

    @DynamicPropertySource
    static void overrideDiscordWebhookBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add(
                "discord.webhook-base-url",
                () -> "http://localhost:8888/"
        );
    }

    @AfterEach
    void tearDown() {
        discordWebhookRepository.deleteAll();
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotCreateWebhookForNoAuthenticatedUser() {
        String payload = """
                {
                    "url": "http://localhost:8888/123/123",
                    "schedules_ids": [1]
                }
                """;

        webClient.post().uri("/api/v1/discord-webhook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotCreateWebhookWithNotAvailableUrl() {
        String payload = """
                {
                    "url": "http://localhost:8888/123/123",
                    "schedules_ids": [1]
                }
                """;

        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .addedBy(userPrincipal.getUser())
                        .url("http://localhost:8888/123/123")
                        .build()
        );

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/discord-webhook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldNotCreateWebhookWithNotExistingSchedules() {
        String payload = """
                {
                    "url": "http://localhost:8888/123/123",
                    "schedules_ids": [1]
                }
                """;

        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/discord-webhook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenDiscordApiReturnedError() {
        stubFor(post("/web-hook-url")
                .willReturn(unauthorized()));

        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(userPrincipal.getUser())
                .build());

        String payload = """
                {
                    "url": "http://localhost:8888/web-hook-url",
                    "schedules_ids": [%d]
                }
                """.formatted(schedule.getId());

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/discord-webhook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        List<DiscordWebhook> allWebhooks =
                discordWebhookRepository.findAll();

        assertThat(allWebhooks).isEmpty();
    }

    @Test
    void shouldCreateDiscordWebhook() {
        stubFor(post(urlPathEqualTo("/web-hook-url"))
                .willReturn(ok()));

        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(userPrincipal.getUser())
                .build());

        String payload = """
                {
                    "url": "http://localhost:8888/web-hook-url",
                    "schedules_ids": [%d]
                }
                """.formatted(schedule.getId());

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/discord-webhook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        List<DiscordWebhook> allWebhooks =
                discordWebhookRepository.findAll();

        assertThat(allWebhooks).hasSize(1);
        assertThat(allWebhooks.get(0).getUrl())
                .isEqualTo("http://localhost:8888/web-hook-url");
    }

    @Test
    void shouldNotDeleteWebhookForNoAuthenticatedUser() {
        webClient.delete().uri("/api/v1/discord-webhook/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotDeleteNotExistingWebhook() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/discord-webhook/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotDeleteForUserWhoIsNotAdminNorOwnerOfWebhook() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_ADMIN,
                true
        );

        UserPrincipal otherUserPrincipal = new UserPrincipal(
                userService.createUser(
                        "email-1@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        DiscordWebhook discordWebhook = discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .url("http://discord.com/api")
                        .addedBy(user)
                        .build());

        String token = jwtService.createToken(otherUserPrincipal);

        webClient.delete().uri("/api/v1/discord-webhook/" + discordWebhook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldDeleteForUserWhoAdminAndNotTheOwnerOfWebhook() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_ADMIN,
                true
        );

        UserPrincipal otherUserPrincipal = new UserPrincipal(
                userService.createUser(
                        "email-1@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        DiscordWebhook discordWebhook = discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .url("http://discord.com/api")
                        .addedBy(user)
                        .build());

        String token = jwtService.createToken(otherUserPrincipal);

        webClient.delete().uri("/api/v1/discord-webhook/" + discordWebhook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        assertThat(discordWebhookRepository.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteForUserTheOwnerOfWebhook() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        DiscordWebhook discordWebhook = discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .url("http://discord.com/api")
                        .addedBy(userPrincipal.getUser())
                        .build());

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/discord-webhook/" + discordWebhook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        assertThat(discordWebhookRepository.findAll()).isEmpty();
    }
}
