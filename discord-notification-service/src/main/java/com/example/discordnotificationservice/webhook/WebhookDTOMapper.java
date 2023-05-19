package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.discord.DiscordWebhookService;
import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
