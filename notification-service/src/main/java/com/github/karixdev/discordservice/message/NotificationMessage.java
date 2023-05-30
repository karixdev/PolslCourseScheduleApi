package com.github.karixdev.discordservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.discordservice.dto.DiscordWebhook;
import com.github.karixdev.discordservice.dto.Embedded;

import java.util.List;

public record NotificationMessage(
        @JsonProperty("content")
        String content,
        @JsonProperty("embeds")
        List<Embedded> embeds,
        @JsonProperty("discordWebhook")
        DiscordWebhook discordWebhook
) {}