package com.github.karixdev.notificationservice.consumer;

import com.github.karixdev.notificationservice.ContainersEnvironment;
import com.github.karixdev.notificationservice.dto.DiscordWebhook;
import com.github.karixdev.notificationservice.message.NotificationMessage;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.notificationservice.props.NotificationMQProperties.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
class NotificationConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    WireMockServer wm;

    @DynamicPropertySource
    static void overrideDiscordApiBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        wm = new WireMockServer(9999);
        wm.start();

        rabbitAdmin.purgeQueue(NOTIFICATION_QUEUE, true);
    }

    @Test
    void shouldConsumeNotificationMessage() {
        NotificationMessage message = new NotificationMessage(
                "content",
                List.of(),
                new DiscordWebhook(
                        "discordId",
                        "token"
                )
        );

        wm.stubFor(
                post(urlPathEqualTo("/webhooks/%s/%s".formatted("discordId", "token")))
                        .willReturn(ok())
        );

        rabbitTemplate.convertAndSend(
                NOTIFICATION_EXCHANGE,
                NOTIFICATION_ROUTING_KEY,
                message
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    int count = (int) rabbitAdmin
                            .getQueueProperties(NOTIFICATION_QUEUE)
                            .get("QUEUE_MESSAGE_COUNT");

                    assertThat(count).isEqualTo(0);
                });
    }
}