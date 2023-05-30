package com.example.discordnotificationservice.client;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.exception.client.ServiceClientException;
import com.example.discordnotificationservice.exception.client.ServiceServerException;
import com.example.discordnotificationservice.exception.notification.NotificationServiceClientException;
import com.example.discordnotificationservice.exception.notification.NotificationServiceServerException;
import com.example.discordnotificationservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ContextConfiguration(classes = {WebClientTestConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
public class NotificationServiceClientTest extends ContainersEnvironment {
    @Autowired
    NotificationServiceClient underTest;

    @DynamicPropertySource
    static void overrideNotificationServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "notification-service.base-url",
                () -> "http://localhost:9999");
    }

    @Test
    void shouldThrowNotificationServiceServerExceptionWhenReceived5xxStatusCode() {
        stubFor(
                post(urlPathEqualTo("/api/notifications/discordId/token"))
                        .willReturn(serverError())
        );

        assertThatThrownBy(() -> underTest.sendWelcomeMessage(
                "discordId",
                "token"
        )).isInstanceOf(ServiceServerException.class);
    }

    @Test
    void shouldThrowNotificationServiceClientExceptionWhenReceived4xxStatusCode() {
        stubFor(
                post(urlPathEqualTo("/api/notifications/discordId/token"))
                        .willReturn(badRequest())
        );

        assertThatThrownBy(() -> underTest.sendWelcomeMessage(
                "discordId",
                "token"
        )).isInstanceOf(ServiceClientException.class);
    }

    @Test
    void shouldRetrieveSendWelcomeMessage() {
        stubFor(
                post(urlPathEqualTo("/api/notifications/discordId/token"))
                        .willReturn(noContent())
        );

        ResponseEntity<Void> result = underTest.sendWelcomeMessage(
                "discordId",
                "token"
        );

        assertThat(result.getStatusCode().value()).isEqualTo(204);
    }
}
