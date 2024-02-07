package com.github.karixdev.webhookservice.mapper;

import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookMapperTest {

	WebhookMapper underTest = new WebhookMapper();

	@Test
	void GivenWebhook_WhenMapToResponse_ThenWebhookIsMappedToResponse() {
		// Given
		Webhook webhook = Webhook.builder()
				.id("id")
				.addedBy("addedBy")
				.schedulesIds(Set.of(UUID.randomUUID()))
				.discordWebhookUrl("url")
				.build();

		// When
		WebhookResponse result = underTest.mapToResponse(webhook);

		// Then
		assertThat(result.id()).isEqualTo(webhook.getId());
		assertThat(result.addedBy()).isEqualTo(webhook.getAddedBy());
		assertThat(result.schedulesIds()).isEqualTo(webhook.getSchedulesIds());
		assertThat(result.discordWebhookUrl()).isEqualTo(webhook.getDiscordWebhookUrl());
	}

	@Test
	void GivenWebhookPage_WheMapToResponsePage_ThenReturnsWebhookResponsePage() {
		// Given
		Webhook webhook = Webhook.builder()
				.id("id")
				.addedBy("addedBy")
				.schedulesIds(Set.of(UUID.randomUUID()))
				.discordWebhookUrl("url")
				.build();

		Page<Webhook> page = new PageImpl<>(List.of(webhook));

		// When
		Page<WebhookResponse> result = underTest.mapToResponsePage(page);

		// Then
		Page<WebhookResponse> expected = new PageImpl<>(List.of(
				WebhookResponse.builder()
						.id(webhook.getId())
						.addedBy(webhook.getAddedBy())
						.schedulesIds(webhook.getSchedulesIds())
						.discordWebhookUrl(webhook.getDiscordWebhookUrl())
						.build()
		));

		assertThat(result).isEqualTo(expected);
	}

}