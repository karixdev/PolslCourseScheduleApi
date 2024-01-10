package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.webhookservice.client.DiscordWebhookClient;
import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import com.github.karixdev.webhookservice.exception.InvalidDiscordWebhookUrlFormatException;
import com.github.karixdev.webhookservice.model.DiscordWebhookParameters;
import com.github.karixdev.webhookservice.validator.DiscordWebhookValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordWebhookService {

	private final DiscordWebhookClient client;
	private final DiscordWebhookValidator validator;

	private final String welcomeMessageContent;

	public DiscordWebhookService(
			DiscordWebhookClient client,
			DiscordWebhookValidator validator,
			@Value("${discord-webhook.welcome-message}") String welcomeMessage
	) {
		this.client = client;
		this.validator = validator;
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
			log.error("Discord API returned client error while checking webhook existence: {}", ex.getMessage(), ex);
			return false;
		}
	}

	public void send(String id, String token, DiscordWebhookRequest request) {
		client.send(id, token, request);
	}

	public DiscordWebhookParameters getParametersFromUrl(String url) {
		if (!validator.isUrlValid(url)) {
			throw new InvalidDiscordWebhookUrlFormatException("discordWebhookUrl");
		}

		String[] split = url.split("/");

		String discordId = split[5];
		String token = split[6];

		return new DiscordWebhookParameters(discordId, token);
	}

}
