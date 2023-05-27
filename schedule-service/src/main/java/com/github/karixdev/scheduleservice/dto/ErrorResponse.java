package com.github.karixdev.scheduleservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("status")
        Integer status,
        @JsonProperty("message")
        String message
) {}
