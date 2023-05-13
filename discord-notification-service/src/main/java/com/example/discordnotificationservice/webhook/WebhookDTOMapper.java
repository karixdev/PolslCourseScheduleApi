package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import org.springframework.stereotype.Component;

@Component
public class WebhookDTOMapper {
    public WebhookResponse map(Webhook discordWebhook) {
        return new WebhookResponse(
                discordWebhook.getId(),
                discordWebhook.getDiscordId(),
                discordWebhook.getDiscordToken(),
                discordWebhook.getSchedules()
        );
    }
}
