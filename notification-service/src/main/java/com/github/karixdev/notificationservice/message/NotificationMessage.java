package com.github.karixdev.notificationservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.notificationservice.dto.DiscordWebhook;
import com.github.karixdev.notificationservice.dto.Embedded;

import java.util.List;

public record NotificationMessage(
        @JsonProperty("content")
        String content,
        @JsonProperty("embeds")
        List<Embedded> embeds,
        @JsonProperty("discordWebhook")
        DiscordWebhook discordWebhook
) {}