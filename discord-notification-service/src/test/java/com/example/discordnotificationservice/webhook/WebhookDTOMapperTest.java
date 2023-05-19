package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.discord.DiscordWebhookService;
import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookDTOMapperTest {
    @InjectMocks
    WebhookDTOMapper underTest;

    @Mock
    DiscordWebhookService discordWebhookService;

    @Test
    void GivenDiscordWebhook_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        DiscordWebhook discordWebhook = new DiscordWebhook("discordId", "token");
        Webhook webhook = new Webhook(
                "12345",
                discordWebhook,
                "addedBy",
                Set.of(scheduleId)
        );

        when(discordWebhookService.transformDiscordWebhookIntoUrl(eq(discordWebhook)))
                .thenReturn("https://discord.com/api/webhooks/discordId/token");

        // When
        WebhookResponse response = underTest.map(webhook);

        // Then
        assertThat(response.id()).isEqualTo("12345");
        assertThat(response.url()).isEqualTo("https://discord.com/api/webhooks/discordId/token");
        assertThat(response.schedules()).containsExactly(scheduleId);
    }
}