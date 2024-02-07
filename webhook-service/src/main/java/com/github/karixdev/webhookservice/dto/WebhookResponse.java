package com.github.karixdev.webhookservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record WebhookResponse(
		@JsonProperty("id")
		String id,
		@JsonProperty("addedBy")
		String addedBy,
		@JsonProperty("schedulesIds")
		Set<UUID> schedulesIds,
		@JsonProperty("discordWebhookUrl")
		String discordWebhookUrl
) {}
