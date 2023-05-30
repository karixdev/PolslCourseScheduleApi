package com.example.discordnotificationservice.consumer;

import com.example.discordnotificationservice.service.DiscordWebhookService;
import com.example.discordnotificationservice.message.ScheduleUpdateMessage;
import com.example.discordnotificationservice.document.Webhook;
import com.example.discordnotificationservice.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.discordnotificationservice.props.ScheduleMQProperties.SCHEDULE_UPDATE_QUEUE;

@Component
@RequiredArgsConstructor
public class ScheduleConsumer {
    private final WebhookService webhookService;
    private final DiscordWebhookService discordWebhookService;

    @RabbitListener(queues = SCHEDULE_UPDATE_QUEUE)
    private void listenForScheduleUpdate(ScheduleUpdateMessage message) {
        List<Webhook> webhooks = webhookService.findBySchedule(message.id());
        System.out.println(message.id());

        webhooks.stream()
                .map(Webhook::getDiscordWebhook)
                .forEach(discordWebhook -> discordWebhookService.sendScheduleUpdateNotification(
                        discordWebhook,
                        message.name()
                ));
    }
}
