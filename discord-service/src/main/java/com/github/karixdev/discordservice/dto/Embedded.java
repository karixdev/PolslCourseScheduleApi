package com.github.karixdev.discordservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Embedded(
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("color")
        Integer color
) {}
