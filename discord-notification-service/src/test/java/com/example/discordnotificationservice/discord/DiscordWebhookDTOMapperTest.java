package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordWebhookDTOMapperTest {
    DiscordWebhookDTOMapper underTest = new DiscordWebhookDTOMapper();

    @Test
    void GivenDiscordWebhook_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        UUID id = UUID.randomUUID();
        DiscordWebhook discordWebhook = new DiscordWebhook(
                "12345",
                "api123",
                "token123",
                "addedBy",
                Set.of(id)
        );

        // When
        DiscordWebhookResponse response = underTest.map(discordWebhook);

        // Then
        assertThat(response.id()).isEqualTo("12345");
        assertThat(response.discordApiId()).isEqualTo("api123");
        assertThat(response.discordToken()).isEqualTo("token123");
        assertThat(response.schedules()).containsExactly(id);
    }
}