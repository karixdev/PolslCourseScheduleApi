package com.github.karixdev.commonservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        @JsonProperty("status")
        Integer status,
        @JsonProperty("error")
        String error,
        @JsonProperty("message")
        String message
) {
        public ErrorResponse(HttpStatus httpStatus, String message) {
                this(httpStatus.value(), httpStatus.toString(), message);
        }
}
