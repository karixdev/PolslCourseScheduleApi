package com.github.karixdev.discordnotificationservice.mapper;

import com.github.karixdev.discordnotificationservice.service.DiscordWebhookService;
import com.github.karixdev.discordnotificationservice.document.Webhook;
import com.github.karixdev.discordnotificationservice.dto.WebhookResponse;
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
