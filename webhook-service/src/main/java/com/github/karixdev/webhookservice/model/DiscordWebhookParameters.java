package com.github.karixdev.webhookservice.model;

import lombok.Builder;

@Builder
public record DiscordWebhookParameters(
		String id,
		String token
) {}
