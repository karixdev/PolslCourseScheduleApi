package com.github.karixdev.webhookservice.dto;

import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record WebhookRequest(
		String discordWebhookUrl,
		Set<UUID> schedulesIds
) {}
