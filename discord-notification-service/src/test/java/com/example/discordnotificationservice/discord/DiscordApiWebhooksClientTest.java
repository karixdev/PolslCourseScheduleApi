package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.discord.dto.DiscordMessageRequest;
import com.example.discordnotificationservice.discord.exception.DiscordApiException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
class DiscordApiWebhooksClientTest extends ContainersEnvironment {
    @Autowired
    DiscordApiWebhooksClient underTest;

    @DynamicPropertySource
    static void overrideDiscordApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
    }

    @Test
    void shouldThrowDiscordApiExceptionWhenApiReturnsError() {
        stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("123", "token")))
                        .willReturn(badRequest())
        );

        assertThatThrownBy(() -> underTest.sendMessage(
                        "123",
                        "token",
                        new DiscordMessageRequest("Message")
                )
        ).isInstanceOf(DiscordApiException.class);
    }

    @Test
    void shouldReturnNoContentResponseStatusWhenSendMessage() {
        stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("123", "token")))
                        .willReturn(noContent())
        );

        var result = underTest.sendMessage(
                "123",
                "token",
                new DiscordMessageRequest(
                        "Message"
                )
        );

        assertThat(result.getStatusCode()
                .isSameCodeAs(HttpStatusCode
                        .valueOf(204))).isTrue();
    }
}