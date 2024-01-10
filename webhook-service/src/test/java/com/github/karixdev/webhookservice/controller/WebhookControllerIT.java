package com.github.karixdev.webhookservice.controller;

import com.github.karixdev.webhookservice.ContainersEnvironment;
import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.repository.WebhookRepository;
import com.github.karixdev.webhookservice.testconfig.WebClientTestConfig;
import com.github.karixdev.webhookservice.utils.KeycloakUtils;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {WebClientTestConfig.class})
@WireMockTest(httpPort = 9999)
class WebhookControllerIT extends ContainersEnvironment {

	@Autowired
	WebTestClient webClient;

	@Autowired
	WebhookRepository webhookRepository;

	@DynamicPropertySource
	static void overrideDiscordWebhookProperties(DynamicPropertyRegistry registry) {
		registry.add("discord-webhook.base-url", () -> "http://localhost:9999/api");
	}

	@DynamicPropertySource
	static void overrideScheduleServiceProperties(DynamicPropertyRegistry registry) {
		registry.add("schedule-service.base-url", () -> "http://localhost:9999");
	}

	@AfterEach
	void tearDown() {
		webhookRepository.deleteAll();
	}

	@Test
	void shouldNotAllowUnauthorizedUserToCreateWebhook() {
		webClient.post().uri("/api/webhooks")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotCreateWebhookWithDuplicatedDiscordWebhookUrl() {
		String url = "http://localhost:9999/api/webhooks/123/abc";

		Webhook webhook = Webhook.builder()
				.schedulesIds(Set.of(UUID.randomUUID()))
				.addedBy("user")
				.discordWebhookUrl(url)
				.build();
		webhookRepository.save(webhook);

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(UUID.randomUUID(), url);

		webClient.post().uri("/api/webhooks")
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(webhookRepository.findAll()).containsExactly(webhook);
	}

	@Test
	void shouldNotCreateWebhookWithInvalidDiscordWebhookUrl() {
		String url = "http://localhost:9999/api/webhooks/abc123/abc%";

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(UUID.randomUUID(), url);

		webClient.post().uri("/api/webhooks")
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(webhookRepository.findAll()).isEmpty();
	}

	@Test
	void shouldNotCreateWebhookWithNotExistingDiscordWebhookUrl() {
		String url = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, url);

		stubFor(
				post(urlPathEqualTo("/api/webhooks/123/abc")).willReturn(badRequest())
		);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(scheduleId.toString()))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody("""
										[
											{
												"id": "%s"
											}
										]
										""".formatted(scheduleId))
						)
		);

		webClient.post().uri("/api/webhooks")
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(webhookRepository.findAll()).isEmpty();
	}

	@Test
	void shouldNotCreateWebhookWithNotExistingScheduleId() {
		String url = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, url);

		stubFor(
				post(urlPathEqualTo("/api/webhooks/123/abc")).willReturn(noContent())
		);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(scheduleId.toString()))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody("[]")
						)
		);

		webClient.post().uri("/api/webhooks")
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();

		assertThat(webhookRepository.findAll()).isEmpty();
	}

	@Test
	void shouldCreateWebhook() {
		String url = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, url);

		stubFor(
				post(urlPathEqualTo("/api/webhooks/123/abc")).willReturn(noContent())
		);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(scheduleId.toString()))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody("""
										[
											{
												"id": "%s",
												"semester": 3,
												"name": "Plan",
												"groupNumber": 4
											}
										]
										""".formatted(scheduleId))
						)
		);

		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		webClient.post().uri("/api/webhooks")
				.header("Authorization", bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.schedulesIds").isArray()
				.jsonPath("$.schedulesIds").isNotEmpty()
				.jsonPath("$.schedulesIds.[0]").isEqualTo(scheduleId.toString())
				.jsonPath("$.addedBy").isEqualTo(userId)
				.jsonPath("$.discordWebhookUrl").isEqualTo(url);

		List<Webhook> results = webhookRepository.findAll();
		assertThat(results).hasSize(1);

		Webhook webhook = results.get(0);
		assertThat(webhook.getId()).isNotEmpty();
		assertThat(webhook.getDiscordWebhookUrl()).isEqualTo(url);
		assertThat(webhook.getAddedBy()).isEqualTo(userId);
		assertThat(webhook.getSchedulesIds()).isEqualTo(Set.of(scheduleId));
	}

	private static String getUserBearer() {
		return "Bearer %s".formatted(KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl()));
	}

	private static String getUserId(String bearer) {
		String token = bearer.substring("Bearer ".length());
		return KeycloakUtils.getUserId(keycloakContainer.getAuthServerUrl(), token);
	}

}