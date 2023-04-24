package com.example.discordnotificationservice.schedule;

import com.example.discordnotificationservice.ContainersEnvironment;
import com.example.discordnotificationservice.testconfig.WebClientTestConfig;
import com.example.discordnotificationservice.schedule.dto.ScheduleResponse;
import com.example.discordnotificationservice.schedule.exception.ScheduleClientException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ContextConfiguration(classes = {WebClientTestConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 9999)
public class ScheduleClientTest extends ContainersEnvironment {
    @Autowired
    ScheduleClient underTest;

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @Test
    void shouldThrowScheduleClientExceptionWhenReceivedErrorStatusCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Set<UUID> ids = Set.of(id1, id2);

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id1.toString(),
                                id2.toString()
                        ))
                        .willReturn(badRequest())
        );

        assertThatThrownBy(() -> underTest.findSelected(ids))
                .isInstanceOf(ScheduleClientException.class);
    }

    @Test
    void shouldRetrieveScheduleResponses() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Set<UUID> ids = Set.of(id1, id2);

        stubFor(
                get(urlPathEqualTo("/api/schedules"))
                        .withQueryParam("ids", havingExactly(
                                id1.toString(),
                                id2.toString()
                        ))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        [
                                            {
                                                "id": "%s"
                                            },
                                            {
                                                "id": "%s"
                                            }
                                        ]
                                        """.formatted(id1, id2)
                                )
                        )
        );

        Set<ScheduleResponse> result = underTest.findSelected(ids);

        assertThat(result).contains(
                new ScheduleResponse(id1)
        );
        assertThat(result).contains(
                new ScheduleResponse(id2)
        );
    }
}
