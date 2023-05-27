package com.github.karixdev.scheduleservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ScheduleUpdateRequestMessage(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("type")
        Integer type,
        @JsonProperty("plan_polsl_id")
        Integer planPolslId,
        @JsonProperty("wd")
        Integer wd
) {}
