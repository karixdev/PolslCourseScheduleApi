package com.github.karixdev.webhookservice.producer;

import com.github.karixdev.webhookservice.document.DiscordWebhook;
import com.github.karixdev.webhookservice.dto.Embedded;
import com.github.karixdev.webhookservice.message.NotificationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static com.github.karixdev.webhookservice.props.NotificationMQProperties.NOTIFICATION_EXCHANGE;
import static com.github.karixdev.webhookservice.props.NotificationMQProperties.NOTIFICATION_ROUTING_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {
    @InjectMocks
    NotificationProducer underTest;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void GivenScheduleNameAndDiscordWebhook_WhenProduceNotificationMessage_ThenProducesCorrectMessageToCorrectQueueWithCorrectRoutingKey() {
        // Given
        String scheduleName = "scheduleName";
        DiscordWebhook discordWebhook = new DiscordWebhook(
                "discordId",
                "token"
        );

        // When
        underTest.produceNotificationMessage(scheduleName, discordWebhook);

        // Then
        NotificationMessage expectedMessage = new NotificationMessage(
                List.of(new Embedded(
                        "Schedule update",
                        "Schedule scheduleName has been updated",
                        10360031
                )),
                discordWebhook
        );

        verify(rabbitTemplate).convertAndSend(
                eq(NOTIFICATION_EXCHANGE),
                eq(NOTIFICATION_ROUTING_KEY),
                eq(expectedMessage)
        );
    }

}