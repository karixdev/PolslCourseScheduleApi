package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.webhookservice.client.DiscordWebhookClient;
import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiscordWebhookService {

	private final DiscordWebhookClient client;
	private final String welcomeMessageContent;

	public DiscordWebhookService(
			DiscordWebhookClient client,
			@Value("${discord-webhook.welcome-message}") String welcomeMessage
	) {
		this.client = client;
		this.welcomeMessageContent = welcomeMessage;
	}

	public boolean doesWebhookExist(String id, String token) {
		try {
			DiscordWebhookRequest welcomeMsg = DiscordWebhookRequest.builder()
					.content(welcomeMessageContent)
					.build();

			client.send(id, token, welcomeMsg);

			return true;
		} catch (HttpServiceClientException ex) {
			return false;
		}
	}

	public void send(String id, String token, DiscordWebhookRequest request) {
		client.send(id, token, request);
	}

}
