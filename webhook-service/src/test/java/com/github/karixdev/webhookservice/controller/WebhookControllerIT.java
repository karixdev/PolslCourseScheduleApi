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
import java.util.stream.IntStream;

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

	@Test
	void shouldNotRetrieveWebhooksForUnauthorizedUser() {
		webClient.get().uri("/api/webhooks")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotRetrieveWebhooksGivenInvalidPaginationParams() {
		webClient.get().uri("/api/webhooks?page=-1&pageSize=0")
				.header("Authorization", getUserBearer())
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void shouldRetrievePaginatedListOfWebhooksPaginationParams() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		seedDBWithWebhooks(userId, 10, 0);

		webClient.get().uri("/api/webhooks")
				.header("Authorization", bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalElements").isEqualTo(10)
				.jsonPath("$.totalPages").isEqualTo(1)
				.jsonPath("$.number").isEqualTo(0)
				.jsonPath("$.numberOfElements").isEqualTo(10)
				.jsonPath("$.content").isNotEmpty();
	}

	@Test
	void shouldRetrievePaginatedListOfWebhooksAddedByUser() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		seedDBWithWebhooks(userId, 10, 0);
		seedDBWithWebhooks("otherUserId", 10, 0);

		webClient.get().uri("/api/webhooks?page=0&pageSize=5")
				.header("Authorization", bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalElements").isEqualTo(10)
				.jsonPath("$.totalPages").isEqualTo(2)
				.jsonPath("$.number").isEqualTo(0)
				.jsonPath("$.numberOfElements").isEqualTo(5)
				.jsonPath("$.content").isNotEmpty()
				.jsonPath("$.content.[0].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[1].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[2].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[3].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[4].addedBy").isEqualTo(userId);

		webClient.get().uri("/api/webhooks?page=1&pageSize=5")
				.header("Authorization", bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalElements").isEqualTo(10)
				.jsonPath("$.totalPages").isEqualTo(2)
				.jsonPath("$.number").isEqualTo(1)
				.jsonPath("$.numberOfElements").isEqualTo(5)
				.jsonPath("$.content").isNotEmpty()
				.jsonPath("$.content.[0].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[1].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[2].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[3].addedBy").isEqualTo(userId)
				.jsonPath("$.content.[4].addedBy").isEqualTo(userId);
	}

	@Test
	void shouldRetrieveAllWebhooksWithPaginationForAdmin() {
		seedDBWithWebhooks("userId1", 1, 0);
		seedDBWithWebhooks("userId2", 1, 1);

		webClient.get().uri("/api/webhooks?page=0&pageSize=5")
				.header("Authorization", getAdminBearer())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.totalElements").isEqualTo(2)
				.jsonPath("$.totalPages").isEqualTo(1)
				.jsonPath("$.number").isEqualTo(0)
				.jsonPath("$.numberOfElements").isEqualTo(2)
				.jsonPath("$.content").isNotEmpty();
	}

	@Test
	void shouldNotAllowUpdatingWebhookWhenUnauthorized() {
		webClient.put().uri("/api/webhooks/id")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotBeAbleToUpdateNotExistingWebhook() {
		webhookRepository.save(Webhook.builder().discordWebhookUrl("url").build());

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(UUID.randomUUID(), "url");

		webClient.put().uri("/api/webhooks/123")
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldNotBeAbleToUpdateNotOwnerWebhook() {
		Webhook webhook = Webhook.builder()
				.addedBy("otherUserId")
				.build();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(UUID.randomUUID(), "url");


		webhookRepository.save(webhook);

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", getUserBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isForbidden();
	}

	@Test
	void shouldNotUpdateWebhookWithAlreadyTakenUrl() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		Webhook webhook = Webhook.builder()
				.addedBy(userId)
				.discordWebhookUrl("url-1")
				.build();

		webhookRepository.saveAll(List.of(
				webhook,
				Webhook.builder()
						.discordWebhookUrl("url-2")
						.build()
		));

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(UUID.randomUUID(), "url-2");

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void shouldNotUpdateWebhookWithNotExistingWebhook() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		Webhook webhook = Webhook.builder()
				.addedBy(userId)
				.discordWebhookUrl("url-1")
				.build();

		webhookRepository.save(webhook);

		String newUrl = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, newUrl);

		stubFor(
				post(urlPathEqualTo("/api/webhooks/123/abc")).willReturn(badRequest())
		);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(scheduleId.toString()))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody("[]")
						)
		);

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void shouldNotUpdateWebhookWithNotExistingSchedule() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		Webhook webhook = Webhook.builder()
				.addedBy(userId)
				.discordWebhookUrl("url-1")
				.build();

		webhookRepository.save(webhook);

		String newUrl = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, newUrl);

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

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void shouldUpdateOwnedWebhookByUser() {
		String bearer = getUserBearer();
		String userId = getUserId(bearer);

		Webhook webhook = Webhook.builder()
				.addedBy(userId)
				.discordWebhookUrl("url-1")
				.build();

		webhookRepository.save(webhook);

		String newUrl = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s"
				}
				""".formatted(scheduleId, newUrl);

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

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(webhook.getId())
				.jsonPath("$.schedulesIds").isArray()
				.jsonPath("$.schedulesIds").isNotEmpty()
				.jsonPath("$.schedulesIds.[0]").isEqualTo(scheduleId.toString())
				.jsonPath("$.addedBy").isEqualTo(userId)
				.jsonPath("$.discordWebhookUrl").isEqualTo(newUrl);

		List<Webhook> webhooks = webhookRepository.findAll();
		assertThat(webhooks).hasSize(1);

		Webhook updatedWebhook = webhooks.get(0);
		assertThat(updatedWebhook.getId()).isEqualTo(webhook.getId());
		assertThat(updatedWebhook.getSchedulesIds()).isEqualTo(Set.of(scheduleId));
		assertThat(updatedWebhook.getAddedBy()).isEqualTo(userId);
		assertThat(updatedWebhook.getDiscordWebhookUrl()).isEqualTo(newUrl);
	}

	@Test
	void shouldUpdateWebhookForAdminWhoIsNotOwner() {
		Webhook webhook = Webhook.builder()
				.addedBy("userId")
				.discordWebhookUrl("url-1")
				.build();

		webhookRepository.save(webhook);

		String newUrl = "http://localhost:9999/api/webhooks/123/abc";
		UUID scheduleId = UUID.randomUUID();
		String newOwner = "newOwner";

		String body = """
				{
					"schedulesIds": ["%s"],
					"discordWebhookUrl": "%s",
					"addedBy": "%s"
				}
				""".formatted(scheduleId, newUrl, newOwner);

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

		webClient.put().uri("/api/webhooks/%s".formatted(webhook.getId()))
				.header("Authorization", getAdminBearer())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(webhook.getId())
				.jsonPath("$.schedulesIds").isArray()
				.jsonPath("$.schedulesIds").isNotEmpty()
				.jsonPath("$.schedulesIds.[0]").isEqualTo(scheduleId.toString())
				.jsonPath("$.addedBy").isEqualTo(newOwner)
				.jsonPath("$.discordWebhookUrl").isEqualTo(newUrl);

		List<Webhook> webhooks = webhookRepository.findAll();
		assertThat(webhooks).hasSize(1);

		Webhook updatedWebhook = webhooks.get(0);
		assertThat(updatedWebhook.getId()).isEqualTo(webhook.getId());
		assertThat(updatedWebhook.getSchedulesIds()).isEqualTo(Set.of(scheduleId));
		assertThat(updatedWebhook.getAddedBy()).isEqualTo(newOwner);
		assertThat(updatedWebhook.getDiscordWebhookUrl()).isEqualTo(newUrl);
	}

	private void seedDBWithWebhooks(String author, int count, int idx) {
		webhookRepository.saveAll(
				IntStream.range(0, count)
						.mapToObj(i ->
								Webhook.builder()
										.addedBy(author)
										.discordWebhookUrl("url-%d-%d".formatted(idx, i))
										.schedulesIds(Set.of(UUID.randomUUID()))
										.build()
						)
						.toList()
		);
	}

	private static String getAdminBearer() {
		return "Bearer %s".formatted(KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl()));
	}

	private static String getUserBearer() {
		return "Bearer %s".formatted(KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl()));
	}

	private static String getUserId(String bearer) {
		String token = bearer.substring("Bearer ".length());
		return KeycloakUtils.getUserId(keycloakContainer.getAuthServerUrl(), token);
	}

}