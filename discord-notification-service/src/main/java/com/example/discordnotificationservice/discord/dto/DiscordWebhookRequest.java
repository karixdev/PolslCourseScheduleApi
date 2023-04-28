package com.example.discordnotificationservice.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record DiscordWebhookRequest(
        @JsonProperty("url")
        @NotEmpty
        String url,
        @JsonProperty("schedules")
        @NotEmpty
        Set<UUID> schedules
) {
}
