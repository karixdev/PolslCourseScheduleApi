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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

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

    @Test
    void GivenNotExistingAddedBy_WhenFindByAddedBy_ThenReturnsEmptyPage() {
        // Given
        String addedBy = "userId";
        PageRequest pageRequest = PageRequest.of(0, 10);

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("discordApiId")
                        .token("token")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Page<DiscordWebhook> result = underTest.findByAddedBy(
                addedBy,
                pageRequest);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingAddedByAndPageable_WhenFindByAddedBy_ThenReturnsCorrectPage() {
        // Given
        String addedBy = "userId";
        PageRequest pageRequest1 = PageRequest.of(0, 2);
        PageRequest pageRequest2 = PageRequest.of(1, 2);

        underTest.save(
                DiscordWebhook.builder()
                        .discordApiId("otherDiscordApiId")
                        .token("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.saveAll(
                IntStream.range(1, 4)
                        .mapToObj(i -> DiscordWebhook.builder()
                                .discordApiId("discordApiId" + i)
                                .token("token" + i)
                                .addedBy(addedBy)
                                .schedules(Set.of(UUID.randomUUID()))
                                .build())
                        .toList()
        );

        // When
        Page<DiscordWebhook> result1 = underTest.findByAddedBy(
                addedBy,
                pageRequest1);

        Page<DiscordWebhook> result2 = underTest.findByAddedBy(
                addedBy,
                pageRequest2);

        // Then
        assertThat(result1.getTotalPages()).isEqualTo(2);
        assertThat(result2.getTotalPages()).isEqualTo(2);

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(1);
    }
}