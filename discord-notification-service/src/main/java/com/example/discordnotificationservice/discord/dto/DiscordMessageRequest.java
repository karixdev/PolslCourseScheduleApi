package com.example.discordnotificationservice.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessageRequest(
        @JsonProperty("content")
        String content
) {}
