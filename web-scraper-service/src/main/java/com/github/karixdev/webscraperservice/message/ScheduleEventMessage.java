package com.github.karixdev.webscraperservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleEventMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId,
        @JsonProperty("type")
        Integer type,
        @JsonProperty("plan_polsl_id")
        Integer planPolslId,
        @JsonProperty("wd")
        Integer wd
) {}
