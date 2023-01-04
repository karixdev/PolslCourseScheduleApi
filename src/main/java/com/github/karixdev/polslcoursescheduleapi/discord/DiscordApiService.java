package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookNotWorkingUrlException;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void sendScheduleCoursesUpdateMessage(Schedule schedule) {
        schedule.getDiscordWebhooks().forEach(discordWebhook -> {
            String payload = """
                    {
                      "embeds": [
                        {
                          "title": "Schedule update",
                          "description": "%s",
                          "color": 16724082
                        }
                      ]
                    }
                    """.formatted(schedule.getName());

            webClient.post().uri(discordWebhook.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                        throw new DiscordWebhookNotWorkingUrlException();
                    })
                    .toBodilessEntity()
                    .block();
        });
    }
}
