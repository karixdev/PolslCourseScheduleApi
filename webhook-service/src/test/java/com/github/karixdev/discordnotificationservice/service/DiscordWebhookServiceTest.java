package com.github.karixdev.discordnotificationservice.service;

import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordWebhookServiceTest {
    DiscordWebhookService underTest = new DiscordWebhookService();

    @Test
    void GivenInvalidDiscordWebhookUrl_WhenIsNotValidDiscordWebhookUrl_ThenReturnsTrue() {
        // Given
        String url = "https://discord.com/not-valid";

        // When
        boolean result = underTest.isNotValidDiscordWebhookUrl(url);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void GivenValidDiscordWebhookUrl_WhenIsNotValidDiscordWebhookUrl_ThenReturnsFalse() {
        // Given
        String url = "https://discord.com/api/webhooks/discordId/token";

        // When
        boolean result = underTest.isNotValidDiscordWebhookUrl(url);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenDiscordWebhookUrl_ThenGetDiscordWebhookFromUrl_ThenReturnsCorrectDiscordWebhook() {
        // Given
        String url = "https://discord.com/api/webhooks/discordId/token";

        // When
        DiscordWebhook result = underTest.getDiscordWebhookFromUrl(url);

        // Then
        DiscordWebhook expected = new DiscordWebhook(
                "discordId",
                "token"
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void GivenDiscordWebhook_WhenTransformDiscordWebhookIntoUrl_ThenReturnsCorrectUrl() {
        // Given
        DiscordWebhook discordWebhook = new DiscordWebhook(
                "discordId",
                "token"
        );

        // When
        String result = underTest.transformDiscordWebhookIntoUrl(discordWebhook);

        // Then
        assertThat(result).isEqualTo("https://discord.com/api/webhooks/discordId/token");
    }
}