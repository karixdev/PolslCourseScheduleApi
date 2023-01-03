package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookNotWorkingUrlException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class DiscordApiService {
    private final WebClient webClient;

    public void sendWelcomeMessage(String url) {
        String payload = """
                {
                    "content": "Greetings from PolslCourseScheduleApi"
                }
                """;

        webClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    throw new DiscordWebhookNotWorkingUrlException();
                })
                .toBodilessEntity()
                .block();
    }
}
