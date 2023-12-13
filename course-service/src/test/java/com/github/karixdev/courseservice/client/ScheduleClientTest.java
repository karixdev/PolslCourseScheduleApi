package com.github.karixdev.courseservice.client;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.dto.ScheduleResponse;
import com.github.karixdev.courseservice.exception.ScheduleServiceClientException;
import com.github.karixdev.courseservice.exception.ScheduleServiceServerException;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ContextConfiguration(classes = {WebClientTestConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
class ScheduleClientTest extends ContainersEnvironment {

    @Autowired
    ScheduleClient underTest;

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @Test
    void shouldThrowScheduleServiceServerExceptionWhenReceivedErrorStatusCode() {
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(500))
        );

        assertThatThrownBy(() -> underTest.findById(id))
                .isInstanceOf(ScheduleServiceServerException.class);
    }

    @Test
    void shouldThrowScheduleServiceClientExceptionWhenReceivedErrorStatusCode() {
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(404))
        );

        assertThatThrownBy(() -> underTest.findById(id))
                .isInstanceOf(ScheduleServiceClientException.class);
    }

    @Test
    void shouldRetrieveScheduleResponses() {
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "%s"
                                        }
                                        """.formatted(id)
                                )
                        )
        );

        Optional<ScheduleResponse> result = underTest.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(id);
    }
}
