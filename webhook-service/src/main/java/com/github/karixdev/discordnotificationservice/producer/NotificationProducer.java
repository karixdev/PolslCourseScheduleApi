package com.github.karixdev.discordnotificationservice.producer;

import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import com.github.karixdev.discordnotificationservice.dto.Embedded;
import com.github.karixdev.discordnotificationservice.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.karixdev.discordnotificationservice.props.NotificationMQProperties.NOTIFICATION_EXCHANGE;
import static com.github.karixdev.discordnotificationservice.props.NotificationMQProperties.NOTIFICATION_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public static final int EMBED_COLOR = 10360031;
    public static final String DESCRIPTION = "Schedule %s has been updated";
    public static final String TITLE = "Schedule update";

    public void produceNotificationMessage(String scheduleName, DiscordWebhook discordWebhook) {
        Embedded embedded = new Embedded(
                TITLE,
                DESCRIPTION.formatted(scheduleName),
                EMBED_COLOR
        );
        NotificationMessage message = new NotificationMessage(
                List.of(embedded),
                discordWebhook
        );

        rabbitTemplate.convertAndSend(
                NOTIFICATION_EXCHANGE,
                NOTIFICATION_ROUTING_KEY,
                message
        );
    }
}
