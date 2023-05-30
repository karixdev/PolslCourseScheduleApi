package com.github.karixdev.discordservice.controller;

import com.github.karixdev.discordservice.ContainersEnvironment;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
public class NotificationControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @DynamicPropertySource
    static void overrideDiscordApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
    }

    @DynamicPropertySource
    static void overrideApiKey(DynamicPropertyRegistry registry) {
        registry.add(
                "api-key",
                () -> "myKey");
    }

    @Test
    void shouldNotAllowSendingWelcomeMessageWithoutApiKey() {
        webClient.post().uri("/api/notifications/discordId/token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReceiveStatus500WhenServerErrorOccursWhileSendingWelcomeMessage() {
        stubFor(
                post(urlPathEqualTo("/webhooks/discordId/token"))
                        .willReturn(serverError())
        );

        webClient.post().uri("/api/notifications/discordId/token")
                .header("X-API-KEY", "myKey")
                .exchange()
                .expectStatus().isEqualTo(500);
    }

    @Test
    void shouldReceiveStatus400WhenClientErrorOccursWhileSendingWelcomeMessage() {
        stubFor(
                post(urlPathEqualTo("/webhooks/discordId/token"))
                        .willReturn(notFound())
        );

        webClient.post().uri("/api/notifications/discordId/token")
                .header("X-API-KEY", "myKey")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldSendWelcomeMessage() {
        stubFor(
                post(urlPathEqualTo("/webhooks/discordId/token"))
                        .willReturn(noContent())
        );

        webClient.post().uri("/api/notifications/discordId/token")
                .header("X-API-KEY", "myKey")
                .exchange()
                .expectStatus().isNoContent();
    }
}
