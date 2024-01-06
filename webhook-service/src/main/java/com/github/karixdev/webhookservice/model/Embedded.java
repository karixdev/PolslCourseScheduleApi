package com.github.karixdev.webhookservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Embedded(
		@JsonProperty("title")
		String title,
		@JsonProperty("description")
		String description,
		@JsonProperty("color")
		Integer color
) {}
