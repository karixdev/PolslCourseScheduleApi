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
public class DiscordWebHookControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserService userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    DiscordWebHookRepository discordWebHookRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

    @DynamicPropertySource
    static void overrideDiscordWebHookBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add(
                "discord.web-hook-base-url",
                () -> "http://localhost:8888/"
        );
    }

    @AfterEach
    void tearDown() {
        discordWebHookRepository.deleteAll();
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotCreateWebHookForNoAuthenticatedUser() {
        String payload = """
                {
                    "url": "http://localhost:8888/123/123",
                    "schedules_ids": [1]
                }
                """;

        webClient.post().uri("/api/v1/discord-web-hook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotCreateWebHookWithNotAvailableUrl() {
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

        discordWebHookRepository.save(
                DiscordWebHook.builder()
                        .addedBy(userPrincipal.getUser())
                        .url("http://localhost:8888/123/123")
                        .build()
        );

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/discord-web-hook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldNotCreateWebHookWithNotExistingSchedules() {
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

        webClient.post().uri("/api/v1/discord-web-hook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotCreateDiscordWebHookWhenDiscordApiReturnedError() {
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

        webClient.post().uri("/api/v1/discord-web-hook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        List<DiscordWebHook> allWebHooks =
                discordWebHookRepository.findAll();

        assertThat(allWebHooks).isEmpty();
    }

    @Test
    void shouldCreateDiscordWebHook() {
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

        webClient.post().uri("/api/v1/discord-web-hook")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        List<DiscordWebHook> allWebHooks =
                discordWebHookRepository.findAll();

        assertThat(allWebHooks).hasSize(1);
        assertThat(allWebHooks.get(0).getUrl())
                .isEqualTo("http://localhost:8888/web-hook-url");
    }

    @Test
    void shouldNotDeleteWebHookForNoAuthenticatedUser() {
        webClient.delete().uri("/api/v1/discord-web-hook/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldNotDeleteNotExistingWebHook() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/discord-web-hook/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotDeleteForUserWhoIsNotAdminNorOwnerOfWebHook() {
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

        DiscordWebHook discordWebHook = discordWebHookRepository.save(
                DiscordWebHook.builder()
                        .url("http://discord.com/api")
                        .addedBy(user)
                        .build());

        String token = jwtService.createToken(otherUserPrincipal);

        webClient.delete().uri("/api/v1/discord-web-hook/" + discordWebHook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldDeleteForUserWhoAdminAndNotTheOwnerOfWebHook() {
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

        DiscordWebHook discordWebHook = discordWebHookRepository.save(
                DiscordWebHook.builder()
                        .url("http://discord.com/api")
                        .addedBy(user)
                        .build());

        String token = jwtService.createToken(otherUserPrincipal);

        webClient.delete().uri("/api/v1/discord-web-hook/" + discordWebHook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        assertThat(discordWebHookRepository.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteForUserTheOwnerOfWebHook() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        DiscordWebHook discordWebHook = discordWebHookRepository.save(
                DiscordWebHook.builder()
                        .url("http://discord.com/api")
                        .addedBy(userPrincipal.getUser())
                        .build());

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/discord-web-hook/" + discordWebHook.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        assertThat(discordWebHookRepository.findAll()).isEmpty();
    }
}
