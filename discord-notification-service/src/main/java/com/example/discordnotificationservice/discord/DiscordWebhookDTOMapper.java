package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import org.springframework.stereotype.Component;

@Component
public class DiscordWebhookDTOMapper {
    public DiscordWebhookResponse map(DiscordWebhook discordWebhook) {
        return new DiscordWebhookResponse(
                discordWebhook.getId(),
                discordWebhook.getDiscordApiId(),
                discordWebhook.getDiscordToken(),
                discordWebhook.getSchedules()
        );
    }
}
