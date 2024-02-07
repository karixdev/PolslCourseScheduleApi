package com.github.karixdev.webhookservice.service;

import com.github.karixdev.webhookservice.client.DiscordWebhookClient;
import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import com.github.karixdev.webhookservice.exception.DiscordWebhookApiClientException;
import com.github.karixdev.webhookservice.exception.InvalidDiscordWebhookUrlFormatException;
import com.github.karixdev.webhookservice.model.DiscordWebhookParameters;
import com.github.karixdev.webhookservice.model.Embedded;
import com.github.karixdev.webhookservice.validator.DiscordWebhookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DiscordWebhookServiceTest {

	DiscordWebhookService underTest;

	DiscordWebhookClient client;

	DiscordWebhookValidator validator;

	String welcomeMessageContent;

	@BeforeEach
	void setUp() {
		welcomeMessageContent = "welcome";
		client = mock(DiscordWebhookClient.class);
		validator = mock(DiscordWebhookValidator.class);

		underTest = new DiscordWebhookService(client, validator, welcomeMessageContent);
	}

	@Test
	void GivenIdAndTokenSuchWebhookIsSentSuccessfully_WhenDoesWebhookExist_ThenSendWebhookAndReturnsTrue() {
		// Given
		String id = "id";
		String token = "token";

		DiscordWebhookRequest expectedWelcomeMsg = DiscordWebhookRequest.builder()
				.content(welcomeMessageContent)
				.build();

		// When
		boolean result = underTest.doesWebhookExist(id, token);

		// Then
		assertThat(result).isTrue();
		verify(client).send(id, token, expectedWelcomeMsg);
	}

	@Test
	void GivenIdAndTokenSuchWebhookIsSentUnsuccessfullyAndHttpServiceClientExceptionIsThrown_WhenDoesWebhookExist_ThenReturnsFalse() {
		// Given
		String id = "id";
		String token = "token";

		DiscordWebhookRequest welcomeMsg = DiscordWebhookRequest.builder()
				.content(welcomeMessageContent)
				.build();

		doThrow(DiscordWebhookApiClientException.class)
				.when(client)
				.send(id, token, welcomeMsg);

		// When
		boolean result = underTest.doesWebhookExist(id, token);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	void GivenIdAndTokenAndRequest_WhenSend_ThenWebhookIsSend() {
		// Given
		String id = "id";
		String token = "token";

		DiscordWebhookRequest request = DiscordWebhookRequest.builder()
				.content("content")
				.embeds(List.of(
						Embedded.builder()
								.title("title")
								.color(123)
								.description("desc")
								.build()
				))
				.build();

		// When
		underTest.send(id, token, request);

		// Then
		verify(client).send(id, token, request);
	}

	@Test
	void GivenInvalidDiscordWebhookUrl_WhenGetParametersFromUrl_ThenThrowsInvalidDiscordWebhookUrlFormatException() {
		// Given
		String url = "invalid";

		when(validator.isUrlValid(url)).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.getParametersFromUrl(url))
				.isInstanceOf(InvalidDiscordWebhookUrlFormatException.class);
	}

	@Test
	void GivenValidDiscordWebhookUrl_WhenGetParametersFromUrl_ThenReturnsCorrectDiscordWebhookParameters() {
		// Given
		String url = "https://dc.com/api/webhooks/discordId/token";

		when(validator.isUrlValid(url)).thenReturn(true);

		// When
		DiscordWebhookParameters result = underTest.getParametersFromUrl(url);

		// Then
		assertThat(result).isEqualTo(new DiscordWebhookParameters("discordId", "token"));
	}

}