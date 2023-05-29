package com.github.karixdev.discordservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record DiscordWebhook(
        @JsonProperty("discordId")
        String discordId,
        @JsonProperty("token")
        String token
) {}
