package com.github.karixdev.webhookservice.mapper;

import com.github.karixdev.webhookservice.service.DiscordWebhookService;
import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
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
