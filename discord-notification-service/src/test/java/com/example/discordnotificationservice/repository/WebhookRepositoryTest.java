package com.example.discordnotificationservice.repository;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.document.DiscordWebhook;
import com.example.discordnotificationservice.document.Webhook;
import com.example.discordnotificationservice.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class WebhookRepositoryTest extends ContainersEnvironment {
    @Autowired
    WebhookRepository underTest;

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
    }

    @Test
    void GivenNotExistingAddedBy_WhenFindByAddedBy_ThenReturnsEmptyPage() {
        // Given
        String addedBy = "userId";
        PageRequest pageRequest = PageRequest.of(0, 10);

        underTest.save(
                Webhook.builder()
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Page<Webhook> result = underTest.findByAddedBy(
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
                Webhook.builder()
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.saveAll(
                IntStream.range(1, 4)
                        .mapToObj(i -> Webhook.builder()
                                .addedBy(addedBy)
                                .schedules(Set.of(UUID.randomUUID()))
                                .build())
                        .toList()
        );

        // When
        Page<Webhook> result1 = underTest.findByAddedBy(
                addedBy,
                pageRequest1);

        Page<Webhook> result2 = underTest.findByAddedBy(
                addedBy,
                pageRequest2);

        // Then
        assertThat(result1.getTotalPages()).isEqualTo(2);
        assertThat(result2.getTotalPages()).isEqualTo(2);

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(1);
    }

    @Test
    void GivenDiscordWebhookIdAndToken_FindByDiscordWebhook_ThenReturnsOptionalWithCorrectDocument() {
        // Given
        String discordWebhookId = "id1";
        String discordWebhookToken = "token1";

        DiscordWebhook discordWebhook = new DiscordWebhook(discordWebhookId, discordWebhookToken);

        Webhook expected = underTest.save(
                Webhook.builder()
                        .addedBy("123")
                        .discordWebhook(new DiscordWebhook(
                                discordWebhookId,
                                discordWebhookToken
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .build()
        );

        // When
        Optional<Webhook> result = underTest.findByDiscordWebhook(discordWebhook);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    void GivenSchedule_WhenFindBySchedule_ThenReturnsCorrectList() {
        // Given
        UUID schedule1 = UUID.fromString("5c0db272-eb91-44d9-97bc-4a259a8a2703");
        UUID schedule2 = UUID.fromString("a105e5f0-64ee-4e83-8c37-7db16240120e");
        UUID schedule3 = UUID.fromString("fc8899d0-a1a2-4e58-9726-6e268c5d2740");

        Webhook webhook1 = underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id1",
                                "token1"
                        ))
                        .schedules(Set.of(schedule1))
                        .build()
        );

        Webhook webhook2 = underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .schedules(Set.of(schedule1, schedule2))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id3",
                                "token3"
                        ))
                        .schedules(Set.of(schedule2, schedule3))
                        .build()
        );

        // When
        List<Webhook> result = underTest.findBySchedulesContaining(schedule1);

        // Then
        assertThat(result).containsExactly(webhook1, webhook2);
    }
}