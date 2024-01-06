package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.webhookservice.client.DiscordWebhookClient;
import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import com.github.karixdev.webhookservice.model.Embedded;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DiscordWebhookServiceTest {

	DiscordWebhookService underTest;

	DiscordWebhookClient client;

	String welcomeMessageContent;

	@BeforeEach
	void setUp() {
		welcomeMessageContent = "welcome";
		client = mock(DiscordWebhookClient.class);

		underTest = new DiscordWebhookService(client, welcomeMessageContent);
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

		doThrow(HttpServiceClientException.class)
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

}