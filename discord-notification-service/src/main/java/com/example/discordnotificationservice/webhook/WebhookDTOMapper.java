package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WebhookDTOMapper {
    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";

    public WebhookResponse map(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                discordWebhookToUrl(webhook.getDiscordWebhook()),
                webhook.getSchedules()
        );
    }

    private String discordWebhookToUrl(DiscordWebhook discordWebhook) {
        return UriComponentsBuilder
                .fromUriString(DISCORD_WEBHOOK_URL_PREFIX)
                .pathSegment(
                        discordWebhook.getDiscordId(),
                        discordWebhook.getToken()
                )
                .toUriString();
    }
}
