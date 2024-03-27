package com.github.karixdev.courseservice.client;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.HttpServiceClientServerException;
import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        ScheduleClient.class,
        ScheduleClientConfig.class,
        WebClientTestConfig.class,
        ObservationAutoConfiguration.class
})
@WireMockTest(httpPort = 9999)
class ScheduleClientTest {

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
                .isInstanceOf(HttpServiceClientServerException.class);
    }

    @Test
    void shouldThrowScheduleServiceClientExceptionWhenReceivedErrorStatusCode() {
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(404))
        );

        assertThatThrownBy(() -> underTest.findById(id))
                .isInstanceOf(HttpServiceClientException.class);
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
