package com.github.karixdev.scheduleservice.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("semester")
        Integer semester,
        @JsonProperty("name")
        String name,
        @JsonProperty("group_number")
        Integer groupNumber
) {}
