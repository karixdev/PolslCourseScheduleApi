package com.example.discordnotificationservice.webhook;

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
                "addedBy",
                Set.of(id)
        );

        // When
        WebhookResponse response = underTest.map(discordWebhook);

        // Then
        assertThat(response.id()).isEqualTo("12345");
        assertThat(response.discordId()).isEqualTo("api123");
        assertThat(response.discordToken()).isEqualTo("token123");
        assertThat(response.schedules()).containsExactly(id);
    }
}