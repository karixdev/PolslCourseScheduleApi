package com.github.karixdev.webhookservice.client;

import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.HttpServiceClientServerException;
import com.github.karixdev.webhookservice.config.ScheduleServiceClientConfig;
import com.github.karixdev.webhookservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
		classes = {
				ScheduleServiceClient.class,
				ScheduleServiceClientConfig.class,
				WebClientTestConfig.class,
				ObservationAutoConfiguration.class
		}
)
@WireMockTest(httpPort = 9999)
class ScheduleServiceClientTest {

	@Autowired
	ScheduleServiceClient underTest;

	@DynamicPropertySource
	static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("schedule-service.base-url", () -> "http://localhost:9999");
	}

	@Test
	void GivenSetOfIds_WhenFind_ThenReturnsListOfScheduleResponses() {
		// Given
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		String responseBody = """
				[
				    {
				        "id": "%s",
				        "semester": 1,
				        "name": "Plan1",
				        "groupNumber": 2
				    },
				    {
				        "id": "%s",
				        "semester": 3,
				        "name": "Plan2",
				        "groupNumber": 4
				    }
				]
				""".formatted(id1, id2);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(id1.toString(), id2.toString()))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody(responseBody)
						)
		);

		// When
		Set<ScheduleResponse> result = underTest.find(Set.of(id1, id2));

		// Then
		assertThat(result).containsExactly(
				ScheduleResponse.builder()
						.id(id1)
						.name("Plan1")
						.semester(1)
						.groupNumber(2)
						.build(),
				ScheduleResponse.builder()
						.id(id2)
						.name("Plan2")
						.semester(3)
						.groupNumber(4)
						.build()
		);
	}

	@Test
	void GivenSetOfIdsThatServiceRespondsWith5xxCode_WhenFind_ThenHttpServiceClientServerExceptionIsThrown() {
		// Given
		UUID id = UUID.randomUUID();
		Set<UUID> ids = Set.of(id);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(id.toString()))
						.willReturn(serverError())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.find(ids))
				.isInstanceOf(HttpServiceClientServerException.class);
	}

	@Test
	void GivenSetOfIdsThatServiceRespondsWith4xxCode_WhenFind_ThenHttpServiceClientExceptionIsThrown() {
		// Given
		UUID id = UUID.randomUUID();
		Set<UUID> ids = Set.of(id);

		stubFor(
				get(urlPathEqualTo("/api/schedules"))
						.withQueryParam("ids", havingExactly(id.toString()))
						.willReturn(badRequest())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.find(ids))
				.isInstanceOf(HttpServiceClientException.class);
	}

	@Test
	void GivenId_WhenFindById_ThenReturnsOptionalWithCorrectValue() {
		// Given
		UUID id = UUID.randomUUID();

		String responseBody = """
				{
					"id": "%s",
					"semester": 1,
					"name": "Plan1",
					"groupNumber": 2
				}
				""".formatted(id);

		stubFor(
				get(urlPathEqualTo("/api/schedules/" + id))
						.willReturn(ok()
								.withHeader("Content-Type", "application/json")
								.withBody(responseBody)
						)
		);

		// When
		Optional<ScheduleResponse> result = underTest.findById(id);

		// Then
		assertThat(result)
				.isPresent()
				.contains(
						ScheduleResponse.builder()
								.id(id)
								.name("Plan1")
								.semester(1)
								.groupNumber(2)
								.build()
				);
	}

	@Test
	void GivenIdThatServiceRespondsWith5xxCode_WhenFindById_ThenHttpServiceClientServerExceptionIsThrown() {
		// Given
		UUID id = UUID.randomUUID();

		stubFor(
				get(urlPathEqualTo("/api/schedules/" + id))
						.willReturn(serverError())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.findById(id))
				.isInstanceOf(HttpServiceClientServerException.class);
	}

	@Test
	void GivenIdThatServiceRespondsWith4xxCode_WhenFindById_ThenHttpServiceClientExceptionIsThrown() {
		// Given
		UUID id = UUID.randomUUID();

		stubFor(
				get(urlPathEqualTo("/api/schedules/" + id))
						.willReturn(badRequest())
		);

		// When & Then
		assertThatThrownBy(() -> underTest.findById(id))
				.isInstanceOf(HttpServiceClientException.class);
	}

}