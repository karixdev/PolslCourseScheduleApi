package com.github.karixdev.webhookservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.webhookservice.model.Embedded;
import lombok.Builder;

import java.util.List;

@Builder
public record DiscordWebhookRequest(
		@JsonProperty("content")
		String content,
		@JsonProperty("embeds")
		List<Embedded> embeds
) {}
