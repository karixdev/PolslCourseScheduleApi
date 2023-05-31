package com.github.karixdev.discordnotificationservice.consumer;

import com.github.karixdev.discordnotificationservice.ContainersEnvironment;
import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import com.github.karixdev.discordnotificationservice.document.Webhook;
import com.github.karixdev.discordnotificationservice.dto.Embedded;
import com.github.karixdev.discordnotificationservice.message.CoursesUpdateMessage;
import com.github.karixdev.discordnotificationservice.message.NotificationMessage;
import com.github.karixdev.discordnotificationservice.repository.WebhookRepository;
import com.github.karixdev.discordnotificationservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.discordnotificationservice.props.CourseEventMQProperties.*;
import static com.github.karixdev.discordnotificationservice.props.NotificationMQProperties.NOTIFICATION_QUEUE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
@ContextConfiguration(classes = {WebClientTestConfig.class})
class CourseEventConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    WebhookRepository webhookRepository;

    @DynamicPropertySource
    static void overrideBaseUrls(DynamicPropertyRegistry registry) {
        registry.add(
                "notification-service.base-url",
                () -> "http://localhost:9999");
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        webhookRepository.deleteAll();

        rabbitAdmin.purgeQueue(COURSES_UPDATE_QUEUE, true);
        rabbitAdmin.purgeQueue(NOTIFICATION_QUEUE, true);
    }

    @Test
    void shouldConsumeCourseUpdateMessageAndProduceNotificationMessage() {
        UUID scheduleId = UUID.randomUUID();
        UUID otherScheduleId = UUID.randomUUID();

        DiscordWebhook discordWebhook = new DiscordWebhook(
                "discordId",
                "token"
        );

        webhookRepository.save(
                Webhook.builder()
                        .discordWebhook(discordWebhook)
                        .schedules(Set.of(scheduleId))
                        .addedBy("123")
                        .build()
        );
        webhookRepository.save(
                Webhook.builder()
                        .discordWebhook(discordWebhook)
                        .schedules(Set.of(otherScheduleId))
                        .addedBy("123")
                        .build()
        );

        stubFor(
                get(urlPathEqualTo("/api/schedules/" + scheduleId))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "%s",
                                            "name": "scheduleName"
                                        }
                                        """.formatted(scheduleId)
                                )
                        )
        );

        CoursesUpdateMessage message = new CoursesUpdateMessage(scheduleId);

        rabbitTemplate.convertAndSend(
                COURSES_UPDATE_EXCHANGE,
                COURSES_UPDATE_ROUTING_KEY,
                message
        );

        NotificationMessage expectedMessage = new NotificationMessage(
                List.of(new Embedded(
                        "Schedule update",
                        "Schedule scheduleName has been updated",
                        10360031
                )),
                discordWebhook
        );

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(getNotificationMessage()).isEqualTo(expectedMessage);
        });
    }

    private NotificationMessage getNotificationMessage() {
        var typeReference = new ParameterizedTypeReference<NotificationMessage>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        };

        return rabbitTemplate.receiveAndConvert(NOTIFICATION_QUEUE, typeReference);
    }
}