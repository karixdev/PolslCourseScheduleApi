package com.github.karixdev.webhookservice.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordWebhookValidatorTest {

	DiscordWebhookValidator underTest;

	String baseUrl;

	@BeforeEach
	void setUp() {
		baseUrl = "https://discord.com";
		underTest = new DiscordWebhookValidator(baseUrl);
	}

	@ParameterizedTest
	@MethodSource("invalidUrls")
	void GivenInvalidUrl_WhenIsUrlValid_ThenReturnsFalse(String invalidUrl) {
		// When
		boolean result = underTest.isUrlValid(invalidUrl);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	void GivenValidUrl_WhenIsUrlValid_ThenReturnsTrue() {
		// Given
		String url = "https://discord.com/webhooks/123/abc123_def-456";

		// When
		boolean result = underTest.isUrlValid(url);

		// Then
		assertThat(result).isTrue();
	}

	private static Stream<Arguments> invalidUrls() {
		return Stream.of(
				Arguments.of("https://discord.com"),
				Arguments.of("https://discord.com/webhooks"),
				Arguments.of("https://sth.com/webhooks"),
				Arguments.of("https://discord.com/hooks"),
				Arguments.of("https://discord.com/webhooks/123"),
				Arguments.of("https://discord.com/webhooks/123/fada%20"),
				Arguments.of("https://discord.com/webhooks/123abc/abc123")
		);
	}

}