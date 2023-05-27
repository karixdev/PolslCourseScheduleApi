package com.github.karixdev.scheduleservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleEventMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId
) {}
