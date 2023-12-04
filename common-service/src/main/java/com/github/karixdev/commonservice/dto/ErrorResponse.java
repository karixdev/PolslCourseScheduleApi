package com.github.karixdev.commonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("status")
        Integer status,
        @JsonProperty("message")
        String message
) {}
