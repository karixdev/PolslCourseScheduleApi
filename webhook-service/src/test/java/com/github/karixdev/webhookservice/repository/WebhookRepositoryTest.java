package com.github.karixdev.webhookservice.repository;

import com.github.karixdev.webhookservice.document.Webhook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
class WebhookRepositoryTest {

	@Container
	static final MongoDBContainer mongoDBContainer =
			new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"))
					.withReuse(true);

	@DynamicPropertySource
	static void overrideDBConnectionProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired
	WebhookRepository underTest;

	@Test
	void GivenDiscordWebhookUrlThatNoneWebhookHas_WhenFindByDiscordWebhookUrl_ThenReturnsEmptyOptional() {
		// Given
		String url = "url";

		underTest.save(Webhook.builder().discordWebhookUrl("my-url").build());

		// When
		Optional<Webhook> result = underTest.findByDiscordWebhookUrl(url);

		// Then
		assertThat(result).isEmpty();
	}

	@Test
	void GivenDiscordWebhookUrlFromOneWebhook_WhenFindByDiscordWebhookUrl_ThenReturnsOptionalWithCorrectWebhook() {
		// Given
		String url = "url";

		Webhook webhook = Webhook.builder()
				.discordWebhookUrl(url)
				.build();

		underTest.saveAll(List.of(
				webhook,
				Webhook.builder().discordWebhookUrl("my-url").build()
		));

		// When
		Optional<Webhook> result = underTest.findByDiscordWebhookUrl(url);

		// Then
		assertThat(result).contains(webhook);
	}

}