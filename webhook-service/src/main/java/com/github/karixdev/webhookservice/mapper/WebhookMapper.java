package com.github.karixdev.webhookservice.mapper;

import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class WebhookMapper {

	public WebhookResponse mapToResponse(Webhook webhook) {
		return WebhookResponse.builder()
				.id(webhook.getId())
				.addedBy(webhook.getAddedBy())
				.schedulesIds(webhook.getSchedulesIds())
				.discordWebhookUrl(webhook.getDiscordWebhookUrl())
				.build();
	}

	public Page<WebhookResponse> mapToResponsePage(Page<Webhook> page) {
		return page.map(this::mapToResponse);
	}

}
