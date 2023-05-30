package com.example.discordnotificationservice.message;

import com.example.discordnotificationservice.document.DiscordWebhook;
import com.example.discordnotificationservice.dto.Embedded;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NotificationMessage(
        @JsonProperty("content")
        String content,
        @JsonProperty("embeds")
        List<Embedded> embeds,
        @JsonProperty("discordWebhook")
        DiscordWebhook discordWebhook
) {
    public NotificationMessage(
            List<Embedded> embeds,
            DiscordWebhook discordWebhook
    ) {
        this(null, embeds, discordWebhook);
    }
}