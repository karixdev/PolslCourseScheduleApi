package com.example.discordnotificationservice.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessageRequest(
        @JsonProperty("content")
        String content
) {}
