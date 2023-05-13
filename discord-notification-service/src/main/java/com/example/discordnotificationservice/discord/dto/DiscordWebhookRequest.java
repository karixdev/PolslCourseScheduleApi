package com.example.discordnotificationservice.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordWebhookRequest(
        @JsonProperty("content")
        String content
) {}
