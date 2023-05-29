package com.github.karixdev.discordservice.client;

import com.github.karixdev.discordservice.ContainersEnvironment;
import com.github.karixdev.discordservice.dto.DiscordWebhookRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
class DiscordWebhookClientTest extends ContainersEnvironment {
    @Autowired
    DiscordWebhookClient underTest;

    @DynamicPropertySource
    static void overrideDiscordApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
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
                new DiscordWebhookRequest(
                        "Message"
                )
        );

        assertThat(result.getStatusCode()
                .isSameCodeAs(HttpStatusCode.valueOf(204))).isTrue();
    }
}