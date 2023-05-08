package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
@ContextConfiguration(classes = {WebClientTestConfig.class})
public class DiscordWebhookControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    DiscordWebhookRepository discordWebhookRepository;

    @DynamicPropertySource
    static void overrideBaseUrls(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @AfterEach
    void tearDown() {
        discordWebhookRepository.deleteAll();
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenProvidedInvalidUrl() {
        UUID id1 = UUID.randomUUID();

        String payload = """
                {
                    "url": "https://not-valid-url.com",
                    "schedules": ["%s"]
                }
                """.formatted(id1.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(discordWebhookRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenProvidedUrlWithUnavailableDiscordApiId() {
        UUID id1 = UUID.randomUUID();
        Set<UUID> schedules = Set.of(id1);

        discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .discordApiId("discordApiId")
                        .token("otherToken")
                        .schedules(schedules)
                        .build()
        );

        String payload = """
                {
                    "url": "https://discord.com/api/webhooks/discordApiId/token",
                    "schedules": ["%s"]
                }
                """.formatted(id1.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(discordWebhookRepository.findAll())
                .hasSize(1);
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenProvidedUrlWithUnavailableToken() {
        UUID id1 = UUID.randomUUID();
        Set<UUID> schedules = Set.of(id1);

        discordWebhookRepository.save(
                DiscordWebhook.builder()
                        .discordApiId("otherDiscordApiId")
                        .token("token")
                        .schedules(schedules)
                        .build()
        );

        String payload = """
                {
                    "url": "https://discord.com/api/webhooks/discordApiId/token",
                    "schedules": ["%s"]
                }
                """.formatted(id1.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(discordWebhookRepository.findAll())
                .hasSize(1);
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenProvidedNotExistingSchedules() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id1.toString(),
                                id2.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id1)
                                )
                        )
        );

        String payload = """
                {
                    "url": "https://discord.com/api/webhooks/discordApiId/token",
                    "schedules": ["%s", "%s"]
                }
                """.formatted(id1.toString(), id2.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(discordWebhookRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateDiscordWebhookWhenProvidedNotWorkingWebhookUrl() {
        UUID id1 = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id1.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id1)
                                )
                        )
        );

        stubFor(
                post(urlPathEqualTo("/webhooks/discordApiId/token"))
                        .willReturn(badRequest())
        );

        String payload = """
                {
                    "url": "https://discord.com/api/webhooks/discordApiId/token",
                    "schedules": ["%s"]
                }
                """.formatted(id1.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(discordWebhookRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateDiscordWebhook() {
        UUID id1 = UUID.randomUUID();

        Set<UUID> schedules = Set.of(id1);

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id1.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id1)
                                )
                        )
        );

        stubFor(
                post(urlPathEqualTo("/webhooks/discordApiId/token"))
                        .willReturn(noContent())
        );

        String payload = """
                {
                    "url": "https://discord.com/api/webhooks/discordApiId/token",
                    "schedules": ["%s"]
                }
                """.formatted(id1.toString());

        String token = getAdminToken();

        webClient.post().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        List<DiscordWebhook> resultList =
                discordWebhookRepository.findAll();

        assertThat(resultList)
                .hasSize(1);

        DiscordWebhook result = resultList.get(0);

        assertThat(result.getDiscordApiId())
                 .isEqualTo("discordApiId");
        assertThat(result.getToken())
                .isEqualTo("token");
        assertThat(result.getSchedules())
                .isEqualTo(schedules);
    }

    @Test
    void shouldRetrievePaginatedCoursesForAdmin() {
        String adminToken = getAdminToken();
        String userToken = getUserToken();

        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id)
                                )
                        )
        );

        stubFor(
                post(urlPathMatching("/[A-Za-z0-9]+/[A-Za-z0-9]+/[A-Za-z0-9]+"))
                        .willReturn(noContent())
        );

        seedDatabase(1, 19, adminToken, id);
        seedDatabase(20, 25, userToken, id);

        webClient.get().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + adminToken
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfElements").isEqualTo(10)
                .jsonPath("totalElements").isEqualTo(25)
                .jsonPath("size").isEqualTo(10)
                .jsonPath("pageable.offset").isEqualTo(0)
                .jsonPath("totalPages").isEqualTo(3)
                .jsonPath("first").isEqualTo(true);

        webClient.get().uri("/api/discord-webhooks?page=1")
                .header(
                        "Authorization",
                        "Bearer " + adminToken
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfElements").isEqualTo(10)
                .jsonPath("pageable.offset").isEqualTo(10)
                .jsonPath("pageable.pageNumber").isEqualTo(1);

        webClient.get().uri("/api/discord-webhooks?page=2")
                .header(
                        "Authorization",
                        "Bearer " + adminToken
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfElements").isEqualTo(5)
                .jsonPath("pageable.offset").isEqualTo(20)
                .jsonPath("pageable.pageNumber").isEqualTo(2)
                .jsonPath("last").isEqualTo(true);
    }

    @Test
    void shouldRetrievePaginatedCoursesForUser() {
        String adminToken = getAdminToken();
        String userToken = getUserToken();

        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id)
                                )
                        )
        );

        stubFor(
                post(urlPathMatching("/[A-Za-z0-9]+/[A-Za-z0-9]+/[A-Za-z0-9]+"))
                        .willReturn(noContent())
        );

        seedDatabase(1, 13, userToken, id);
        seedDatabase(14, 2, adminToken, id);

        webClient.get().uri("/api/discord-webhooks")
                .header(
                        "Authorization",
                        "Bearer " + userToken
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfElements").isEqualTo(10)
                .jsonPath("totalElements").isEqualTo(13)
                .jsonPath("size").isEqualTo(10)
                .jsonPath("pageable.offset").isEqualTo(0)
                .jsonPath("totalPages").isEqualTo(2)
                .jsonPath("first").isEqualTo(true);

        webClient.get().uri("/api/discord-webhooks?page=1")
                .header(
                        "Authorization",
                        "Bearer " + userToken
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfElements").isEqualTo(3)
                .jsonPath("pageable.pageNumber").isEqualTo(1)
                .jsonPath("pageable.offset").isEqualTo(10)
                .jsonPath("last").isEqualTo(true);
    }

    private void seedDatabase(int start, int end, String token, UUID scheduleId) {
        IntStream.rangeClosed(start, end)
                .forEach(i -> {
                    String payload = """
                            {
                                "url": "https://discord.com/api/webhooks/discordApiId%d/token%d",
                                "schedules": ["%s"]
                            }
                            """.formatted(i, i, scheduleId.toString());

                    webClient.post().uri("/api/discord-webhooks")
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().isCreated();
                });
    }
}
