package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.discord.DiscordApiService;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebHookNotWorkingUrlException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 8888)
public class DiscordApiServiceTest {
    DiscordApiService underTest;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .build();

        underTest = new DiscordApiService(webClient);
    }

    @Test
    void GivenUrlThatDiscordApiRespondsWith4xxStatus_WhenSendWelcomeMessage_ThenThrowsDiscordWebHookNotWorkingUrlExceptionWithProperMessage() {
        // Given
        String url = "http://localhost:8888/error-route";

        stubFor(post("/error-route")
                .willReturn(unauthorized()));

        // When & Then
        assertThatThrownBy(() -> underTest.sendWelcomeMessage(url))
                .isInstanceOf(DiscordWebHookNotWorkingUrlException.class)
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
}
