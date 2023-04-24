package com.example.discordwebhooksservice.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessageRequest(
        @JsonProperty("content")
        String content
) {}
