package com.example.discordnotificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DiscordWebhookRequest(
        @JsonProperty("content")
        String content,
        @JsonProperty("embeds")
        List<Embedded> embeds
) {
    public DiscordWebhookRequest(String content) {
        this(content, null);
    }

    public DiscordWebhookRequest(Embedded embed) {
        this(null, List.of(embed));
    }
}
