package com.github.karixdev.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordWebhook(
        @JsonProperty("discordId")
        String discordId,
        @JsonProperty("token")
        String token
) {}
