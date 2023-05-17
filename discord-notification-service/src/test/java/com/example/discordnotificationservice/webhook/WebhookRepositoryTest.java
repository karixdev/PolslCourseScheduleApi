package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    void GivenNotExistingWebhookDiscordId_WhenFindByDiscordId_ThenReturnsEmptyOptional() {
        // Given
        String discordApiId = "discordApiId";

        underTest.save(
                Webhook.builder()
                        .discordId("otherDiscordApiId")
                        .discordToken("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<Webhook> result =
                underTest.findByDiscordApiId(discordApiId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingWebhookDiscordId_WhenFindByDiscordId_ThenOptionalWithCorrectDocument() {
        // Given
        String discordApiId = "discordApiId";

        Webhook expected = underTest.save(
                Webhook.builder()
                        .discordId(discordApiId)
                        .discordToken("token")
                        .addedBy("222")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .discordId("otherDiscordApiId")
                        .discordToken("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<Webhook> result =
                underTest.findByDiscordApiId(discordApiId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    void GivenNotExistingWebhookToken_WhenFindByDiscordToken_ThenReturnsEmptyOptional() {
        // Given
        String token = "token";

        underTest.save(
                Webhook.builder()
                        .discordId("discordApiId")
                        .discordToken("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<Webhook> result = underTest.findByToken(token);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingWebhookToken_WhenFindByDiscordToken_ThenOptionalWithCorrectDocument() {
        // Given
        String token = "token";

        Webhook expected = underTest.save(
                Webhook.builder()
                        .discordId("discordApiId")
                        .discordToken(token)
                        .addedBy("222")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .discordId("discordApiId2")
                        .discordToken("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        // When
        Optional<Webhook> result = underTest.findByToken(token);

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
                Webhook.builder()
                        .discordId("discordApiId")
                        .discordToken("token")
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
                        .discordId("otherDiscordApiId")
                        .discordToken("otherToken")
                        .addedBy("111")
                        .schedules(Set.of(UUID.randomUUID()))
                        .build()
        );

        underTest.saveAll(
                IntStream.range(1, 4)
                        .mapToObj(i -> Webhook.builder()
                                .discordId("discordApiId" + i)
                                .discordToken("token" + i)
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
                        .discordId("123")
                        .discordToken("123")
                        .addedBy("123")
                        .discordWebhook(new DiscordWebhook(
                                discordWebhookId,
                                discordWebhookToken
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .discordId("1234")
                        .discordToken("1234")
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .discordId("123")
                        .discordToken("1234")
                        .addedBy("1234")
                        .discordWebhook(new DiscordWebhook(
                                "id2",
                                "token2"
                        ))
                        .build()
        );

        underTest.save(
                Webhook.builder()
                        .discordId("1234")
                        .discordToken("123")
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
}