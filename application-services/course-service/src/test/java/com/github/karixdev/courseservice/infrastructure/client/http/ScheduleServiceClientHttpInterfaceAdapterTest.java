package com.github.karixdev.courseservice.infrastructure.client.http;

import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import com.github.karixdev.courseservice.infrastructure.client.http.config.HttpClientGeneralConfig;
import com.github.karixdev.courseservice.infrastructure.client.http.config.HttpInterfacesScheduleServiceClientConfig;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

import static com.github.karixdev.courseservice.infrastructure.client.http.ScheduleServiceClientHttpInterfaceAdapterTest.HTTP_PORT;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        HttpClientGeneralConfig.class,
        WebClientTestConfig.class,
        ObservationAutoConfiguration.class,
        HttpInterfacesScheduleServiceClientConfig.class,
        ScheduleServiceClientHttpInterfaceAdapter.class
})
@WireMockTest(httpPort = HTTP_PORT)
class ScheduleServiceClientHttpInterfaceAdapterTest {

    static final int HTTP_PORT = 9999;

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:%s".formatted(HTTP_PORT));
    }

    @Autowired
    ScheduleServiceClient underTest;

    @Test
    void GivenIdSuchScheduleServiceRespondsWithError_WhenDoesScheduleWithIdExist_ThenThrowsWebClientResponseException() {
        // Given
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(500))
        );

        // When & Then
        assertThatThrownBy(() -> underTest.doesScheduleWithIdExist(id))
                .isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void GivenNotExistingScheduleId_WhenDoesScheduleWithIdExist_ThenReturnsFalse() {
        // Given
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/queries/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(404))
        );

        // When
        boolean result = underTest.doesScheduleWithIdExist(id);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenScheduleId_WhenDoesScheduleWithIdExist_ThenReturnsTrue() {
        // Given
        UUID id = UUID.randomUUID();

        stubFor(
                get(urlPathEqualTo("/api/schedules/%s".formatted(id)))
                        .willReturn(aResponse().withStatus(200))
        );

        // When
        boolean result = underTest.doesScheduleWithIdExist(id);

        // Then
        assertThat(result).isTrue();
    }

}