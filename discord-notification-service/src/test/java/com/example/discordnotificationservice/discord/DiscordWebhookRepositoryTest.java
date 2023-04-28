package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.ContainersEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class DiscordWebhookRepositoryTest extends ContainersEnvironment {
    @Autowired
    DiscordWebhookRepository underTest;

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
    }

    @Test
    void GivenNotExistingDiscordWebhookDiscordApiId_WhenFindByToken_ThenReturnsEmptyOptional() {
        // Given
        String discordApiId = "discordApiId";

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("otherDiscordApiId")
                        .token("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<DiscordWebhook> result =
                underTest.findByDiscordApiId(discordApiId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingDiscordWebhookDiscordApiId_WhenFindByToken_ThenOptionalWithCorrectDocument() {
        // Given
        String discordApiId = "discordApiId";

        DiscordWebhook expected = underTest.save(
                DiscordWebhook.builder()
                        .discordApiId(discordApiId)
                        .token("token")
                        .addedBy("222")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("otherDiscordApiId")
                        .token("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<DiscordWebhook> result =
                underTest.findByDiscordApiId(discordApiId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    void GivenNotExistingDiscordWebhookToken_WhenFindByToken_ThenReturnsEmptyOptional() {
        // Given
        String token = "token";

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("discordApiId")
                        .token("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<DiscordWebhook> result = underTest.findByToken(token);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingDiscordWebhookToken_WhenFindByToken_ThenOptionalWithCorrectDocument() {
        // Given
        String token = "token";

        DiscordWebhook expected = underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("discordApiId")
                        .token(token)
                        .addedBy("222")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("discordApiId2")
                        .token("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<DiscordWebhook> result = underTest.findByToken(token);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }
}