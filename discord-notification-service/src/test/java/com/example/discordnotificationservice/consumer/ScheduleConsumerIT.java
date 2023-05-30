package com.example.discordnotificationservice.consumer;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.message.ScheduleUpdateMessage;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static com.example.discordnotificationservice.props.ScheduleMQProperties.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
class ScheduleConsumerIT extends ContainersEnvironment {

    @Autowired
    RabbitTemplate template;

    @Autowired
    RabbitAdmin admin;

    WireMockServer wm;

    @DynamicPropertySource
    static void overrideBaseUrls(DynamicPropertyRegistry registry) {
        registry.add(
                "discord-api.base-url",
                () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        wm = new WireMockServer(9999);
        wm.start();

        admin.purgeQueue(SCHEDULE_UPDATE_QUEUE, true);
    }

    @Test
    void shouldSendScheduleNotificationWhenReceivedScheduleUpdateMessage() {
        wm.stubFor(
                post(urlPathEqualTo("/webhooks/discordApiId/token"))
                        .willReturn(noContent())
        );

        template.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_ROUTING_KEY,
                new ScheduleUpdateMessage(
                        UUID.randomUUID(),
                        "name"
                )
        );
    }
}