package com.example.discordnotificationservice.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.UUID;

public record WebhookResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("discord_id")
        String discordId,
        @JsonProperty("discord_token")
        String discordToken,
        @JsonProperty("schedules")
        Set<UUID> schedules
) {}
