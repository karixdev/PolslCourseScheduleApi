package com.example.discordnotificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleResponse(
        @JsonProperty("id")
        UUID id
) {}
