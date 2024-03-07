package com.github.karixdev.scheduleservice.infrastructure.rest.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PublicScheduleResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("group") Integer group
) {}
