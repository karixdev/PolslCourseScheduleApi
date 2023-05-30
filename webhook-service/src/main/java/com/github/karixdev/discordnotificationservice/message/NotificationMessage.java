package com.github.karixdev.discordnotificationservice.message;

import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import com.github.karixdev.discordnotificationservice.dto.Embedded;
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