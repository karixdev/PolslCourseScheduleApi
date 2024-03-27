package com.github.karixdev.scheduleservice.infrastructure.rest.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ScheduleResponse(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("semester")
        Integer semester,
        @JsonProperty("major")
        String major,
        @JsonProperty("group")
        Integer group,
        @JsonProperty("planPolslData")
        PlanPolslDataResponse planPoslData
) {
}
