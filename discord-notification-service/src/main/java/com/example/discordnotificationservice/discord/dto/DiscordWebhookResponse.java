package com.example.discordnotificationservice.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.UUID;

public record DiscordWebhookResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("discord_api_id")
        String discordApiId,
        @JsonProperty("discord_token")
        String discordToken,
        @JsonProperty("schedules")
        Set<UUID> schedules
) {}
