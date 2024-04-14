package com.github.karixdev.courseservice.infrastructure.rest.exception.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ErrorResponse(
        @JsonProperty("message")
        String message,
        @JsonProperty("status")
        Integer status
) {}
