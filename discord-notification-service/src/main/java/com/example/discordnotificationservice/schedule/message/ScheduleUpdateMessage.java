package com.example.discordnotificationservice.schedule.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleUpdateMessage(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("name")
        String name
) {}