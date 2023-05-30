package com.example.discordnotificationservice.mapper;

import com.example.discordnotificationservice.service.DiscordWebhookService;
import com.example.discordnotificationservice.document.Webhook;
import com.example.discordnotificationservice.dto.WebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookDTOMapper {
    private final DiscordWebhookService discordWebhookService;

    public WebhookResponse map(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                discordWebhookService.transformDiscordWebhookIntoUrl(
                        webhook.getDiscordWebhook()
                ),
                webhook.getSchedules()
        );
    }
}
