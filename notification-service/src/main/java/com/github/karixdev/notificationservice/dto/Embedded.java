package com.github.karixdev.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Embedded(
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("color")
        Integer color
) {}
