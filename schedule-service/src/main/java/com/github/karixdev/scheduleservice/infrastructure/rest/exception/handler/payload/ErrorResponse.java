package com.github.karixdev.scheduleservice.infrastructure.rest.exception.handler.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ErrorResponse(
        @JsonProperty("message")
        String message,
        @JsonProperty("status")
        Integer status
) {}
