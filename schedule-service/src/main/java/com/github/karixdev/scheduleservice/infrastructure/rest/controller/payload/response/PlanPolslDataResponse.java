package com.github.karixdev.scheduleservice.infrastructure.rest.controller.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PlanPolslDataResponse(
        @JsonProperty("type")
        Integer type,
        @JsonProperty("id")
        Integer id,
        @JsonProperty("weekDays")
        Integer weekDays
) {
}
