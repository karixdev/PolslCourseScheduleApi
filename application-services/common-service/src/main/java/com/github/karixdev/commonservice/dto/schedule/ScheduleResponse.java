package com.github.karixdev.commonservice.dto.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
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
