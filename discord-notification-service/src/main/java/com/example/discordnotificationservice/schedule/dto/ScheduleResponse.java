package com.example.discordnotificationservice.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleResponse(
        @JsonProperty("id")
        UUID id
) {}
