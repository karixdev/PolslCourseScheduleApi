package com.github.karixdev.webhookservice.client;

import com.github.karixdev.webhookservice.config.DiscordWebhookClientConfig;
import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import com.github.karixdev.webhookservice.exception.DiscordWebhookApiClientException;
import com.github.karixdev.webhookservice.exception.DiscordWebhookApiServerException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = {
		DiscordWebhookClient.class,
		DiscordWebhookClientConfig.class,
		ObservationAutoConfiguration.class
})
@WireMockTest(httpPort = 9999)
class DiscordWebhookClientTest {

	@Autowired
	DiscordWebhookClient underTest;

	@DynamicPropertySource
	static void overrideDiscordWebhookBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("discord-webhook.base-url", () -> "http://localhost:9999");
	}

	@Test
	void GivenDiscordIdAndTokenAndDiscordWebhookRequest_WhenSend_ThenWebhookIsSentAndNothingIsThrown() {
		// Given
		String discordId = "123";
		String token = "abc";
		DiscordWebhookRequest request = DiscordWebhookRequest.builder()
				.content("content")
				.build();

		stubFor(
				post(urlPathEqualTo("/webhooks/%s/%s".formatted(discordId, token)))
						.willReturn(noContent())
		);

		// When & Then
		assertDoesNotThrow(() -> underTest.send(discordId, token, request));
	}

	@Test
	void GivenDiscordIdAndTokenAndDiscordWebhookRequestsSuchDiscordRespondsWith5xxError_WhenSend_ThenDiscordWebhookApiServerException() {
		// Given
		String discordId = "123";
		String token = "abc";
		DiscordWebhookRequest request = DiscordWebhookRequest.builder()
				.content("content")
				.build();

		stubFor(
				post(urlPathEqualTo("/webhooks/%s/%s".formatted(discordId, token)))
						.willReturn(serverError())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.send(discordId, token, request))
				.isInstanceOf(DiscordWebhookApiServerException.class);
	}

	@Test
	void GivenDiscordIdAndTokenAndDiscordWebhookRequestsSuchDiscordRespondsWith4xxError_WhenSend_ThenDiscordWebhookApiClientException() {
		// Given
		String discordId = "123";
		String token = "abc";
		DiscordWebhookRequest request = DiscordWebhookRequest.builder()
				.content("content")
				.build();

		stubFor(
				post(urlPathEqualTo("/webhooks/%s/%s".formatted(discordId, token)))
						.willReturn(badRequest())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.send(discordId, token, request))
				.isInstanceOf(DiscordWebhookApiClientException.class);
	}

}