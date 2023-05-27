package com.github.karixdev.scheduleservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("semester")
        Integer semester,
        @JsonProperty("name")
        String name,
        @JsonProperty("groupNumber")
        Integer groupNumber
) {}
