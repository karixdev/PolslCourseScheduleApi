package com.github.karixdev.webhookservice.matcher;

import com.github.karixdev.webhookservice.document.Webhook;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Objects;

@RequiredArgsConstructor
public class DeepWebhookArgumentMatcher implements ArgumentMatcher<Webhook> {

	private final Webhook webhook;

	public static Webhook deepWebhookEq(Webhook webhook) {
		return Mockito.argThat(new DeepWebhookArgumentMatcher(webhook));
	}

	@Override
	public boolean matches(Webhook o) {
		return Objects.equals(webhook.getId(), o.getId())
				&& Objects.equals(webhook.getAddedBy(), o.getAddedBy())
				&& Objects.equals(webhook.getSchedulesIds(), o.getSchedulesIds())
				&& Objects.equals(webhook.getDiscordWebhookUrl(), o.getDiscordWebhookUrl());
	}
}
