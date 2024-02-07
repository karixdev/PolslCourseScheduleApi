package com.github.karixdev.webhookservice.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DiscordWebhookValidator {

	private final String urlFirstPart;

	private static final String PATH_FIRST_PART = "/webhooks";
	private static final String URL_SECOND_PART_REGEX = "/\\d+/[a-zA-Z0-9_-]+";

	public DiscordWebhookValidator(@Value("${discord-webhook.base-url}") String baseUrl) {
		this.urlFirstPart = baseUrl + PATH_FIRST_PART;
	}

	public boolean isUrlValid(String url) {
		if (!url.startsWith(urlFirstPart)) {
			return false;
		}

		String secondPart = url.substring(urlFirstPart.length());
		return Pattern.matches(URL_SECOND_PART_REGEX, secondPart);
	}

}
