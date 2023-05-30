package com.github.karixdev.discordnotificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.UUID;

public record WebhookResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("url")
        String url,
        @JsonProperty("schedules")
        Set<UUID> schedules
) {}
