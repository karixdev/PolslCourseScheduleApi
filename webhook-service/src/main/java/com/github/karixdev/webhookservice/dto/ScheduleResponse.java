package com.github.karixdev.webhookservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("name")
        String name
) {}
