package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import org.springframework.stereotype.Component;

@Component
public class WebhookDTOMapper {
    public WebhookResponse map(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                webhook.getDiscordId(),
                webhook.getDiscordToken(),
                webhook.getSchedules()
        );
    }
}
