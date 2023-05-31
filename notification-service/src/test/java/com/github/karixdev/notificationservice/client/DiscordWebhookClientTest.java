package com.github.karixdev.notificationservice.client;

import com.github.karixdev.notificationservice.ContainersEnvironment;
import com.github.karixdev.notificationservice.dto.DiscordWebhookRequest;
import com.github.karixdev.notificationservice.exception.DiscordClientException;
import com.github.karixdev.notificationservice.exception.DiscordServerException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void shouldThrowDiscordServerExceptionWhen5xxIsReturned() {
        stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("123", "token")))
                        .willReturn(serverError())
        );

        assertThatThrownBy(() -> underTest.sendMessage(
                "123",
                "token",
                new DiscordWebhookRequest(
                        "Message"
                )
        )).isInstanceOf(DiscordServerException.class);
    }

    @Test
    void shouldThrowDiscordClientExceptionWhen4xxIsReturned() {
        stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("123", "token")))
                        .willReturn(badRequest())
        );

        assertThatThrownBy(() -> underTest.sendMessage(
                "123",
                "token",
                new DiscordWebhookRequest(
                        "Message"
                )
        )).isInstanceOf(DiscordClientException.class);
    }

    @Test
    void shouldReturnNoContentResponseStatusWhenSendMessage() {
        stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("123", "token")))
                        .willReturn(noContent())
        );

        ResponseEntity<Void> result = underTest.sendMessage(
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