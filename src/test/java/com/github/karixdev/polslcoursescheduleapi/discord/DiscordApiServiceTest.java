package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookNotWorkingUrlException;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 8888)
public class DiscordApiServiceTest {
    DiscordApiService underTest;

    Schedule schedule;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .build();

        underTest = new DiscordApiService(webClient);

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("http://localhost:8888/api/error")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        DiscordWebhook otherDiscordWebhook = DiscordWebhook.builder()
                .id(2L)
                .url("http://localhost:8888/api/success")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        schedule.setDiscordWebhooks(Set.of(discordWebhook, otherDiscordWebhook));
    }

    @Test
    void GivenUrlThatDiscordApiRespondsWith4xxStatus_WhenSendWelcomeMessage_ThenThrowsDiscordWebhookNotWorkingUrlExceptionWithProperMessage() {
        // Given
        String url = "http://localhost:8888/error-route";

        stubFor(post("/error-route")
                .willReturn(unauthorized()));

        // When & Then
        assertThatThrownBy(() -> underTest.sendWelcomeMessage(url))
                .isInstanceOf(DiscordWebhookNotWorkingUrlException.class)
                .hasMessage("Provided discord web hook url is not working properly");
    }

    @Test
    void GivenUrlThatDiscordApiRespondsWithSuccessStatus_WhenSendWelcomeMessage_ThenDoesNotThrowAnyException () {
        // Given
        String url = "http://localhost:8888/success-route";

        stubFor(post("/success-route")
                .willReturn(noContent()));

        // When
        underTest.sendWelcomeMessage(url);
    }

    @Test
    void GivenScheduleOneWithWebhookUrlThatDiscordApiRespondsWith4xxStatus_WhenSendScheduleCoursesUpdateMessage_ThenDoesNotThrow() {
        stubFor(post(urlPathEqualTo("/api/error"))
                .willReturn(unauthorized()));

        stubFor(post(urlPathEqualTo("/api/success"))
                .willReturn(noContent()));

        // When
        underTest.sendScheduleCoursesUpdateMessage(schedule);
    }
}
