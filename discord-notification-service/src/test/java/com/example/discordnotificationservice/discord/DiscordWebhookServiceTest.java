package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.Embedded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.discordnotificationservice.discord.DiscordWebhookService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscordWebhookServiceTest {
    @InjectMocks
    DiscordWebhookService underTest;

    @Mock
    DiscordWebhookClient client;

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
    void GivenDiscordWebhook_SendWelcomeMessage_ThenSendsMessageUsingDiscordWebhookClient() {
        // Given
        DiscordWebhook discordWebhook = new DiscordWebhook(
                "discordId",
                "token"
        );

        // When
        underTest.sendWelcomeMessage(discordWebhook);

        // Then
        verify(client).sendMessage(
                eq("discordId"),
                eq("token"),
                eq(new DiscordWebhookRequest("Hello form PolslCourseApi!"))
        );
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

    @Test
    void GivenDiscordWebhookAndScheduleName_WhenSendScheduleUpdateNotification_ThenSendsCorrectMessage() {
        // Given
        DiscordWebhook discordWebhook = new DiscordWebhook(
                "discordId",
                "token"
        );
        String scheduleName = "scheduleName";

        DiscordWebhookRequest request = new DiscordWebhookRequest(
                new Embedded(
                        SCHEDULE_UPDATE_TITLE,
                        SCHEDULE_UPDATE_DESCRIPTION.formatted(scheduleName),
                        EMBED_COLOR
                )
        );

        // When
        underTest.sendScheduleUpdateNotification(discordWebhook, scheduleName);

        // Then
        verify(client).sendMessage(
                eq(discordWebhook.getDiscordId()),
                eq(discordWebhook.getToken()),
                eq(request)
        );
    }
}