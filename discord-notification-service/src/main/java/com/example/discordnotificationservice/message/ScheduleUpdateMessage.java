package com.example.discordnotificationservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleUpdateMessage(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("name")
        String name
) {}