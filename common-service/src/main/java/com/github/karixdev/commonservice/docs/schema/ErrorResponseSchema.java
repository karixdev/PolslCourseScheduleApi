package com.github.karixdev.commonservice.docs.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ErrorResponse")
public record ErrorResponseSchema(
        @JsonProperty("status")
        @Schema(nullable = true)
        Integer status,
        @JsonProperty("error")
        @Schema(nullable = true)
        String error,
        @JsonProperty("message")
        @Schema(nullable = true)
        String message,
        @JsonProperty("constraints")
        @Schema(nullable = true)
        Map<String, String> constraints
) {
}
