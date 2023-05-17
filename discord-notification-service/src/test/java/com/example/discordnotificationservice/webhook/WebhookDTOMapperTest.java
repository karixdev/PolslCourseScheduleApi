package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookDTOMapperTest {
    WebhookDTOMapper underTest = new WebhookDTOMapper();

    @Test
    void GivenDiscordWebhook_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        UUID id = UUID.randomUUID();
        Webhook discordWebhook = new Webhook(
                "12345",
                "api123",
                "token123",
                new DiscordWebhook(
                        "discordId",
                        "token"
                ),
                "addedBy",
                Set.of(id)
        );

        // When
        WebhookResponse response = underTest.map(discordWebhook);

        // Then
        assertThat(response.id()).isEqualTo("12345");
        assertThat(response.url()).isEqualTo("https://discord.com/api/webhooks/discordId/token");
        assertThat(response.schedules()).containsExactly(id);
    }
}