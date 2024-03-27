package com.github.karixdev.commonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.Map;

public record ValidationErrorResponse(
        @JsonProperty("constraints")
        Map<String, String> constraints,
        @JsonProperty("status")
        Integer status,
        @JsonProperty("error")
        String error,
        @JsonProperty("message")
        String message
) {
    public ValidationErrorResponse(Map<String, String> constraints) {
        this(
                constraints,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.toString(),
                "Validation Failed"
        );
    }
}
