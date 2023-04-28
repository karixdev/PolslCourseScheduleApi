package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.dto.DiscordMessageRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import com.example.discordnotificationservice.discord.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.discord.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
    private final DiscordApiWebhooksClient discordApiWebhooksClient;
    private final ScheduleService scheduleService;
    private final DiscordWebhookRepository repository;

    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";
    private static final String WELCOME_MESSAGE = "Hello form PolslCourseApi!";

    @Transactional
    public DiscordWebhookResponse create(DiscordWebhookRequest request, Jwt jwt) {
        String discordWebhookUrl = request.url();

        if (!isValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new InvalidDiscordWebhookUrlException();
        }

        String discordApiId = getDiscordApiIdFromUrl(discordWebhookUrl);
        String token = getTokenFromUrl(discordWebhookUrl);

        Set<UUID> schedules = request.schedules();

        if (!scheduleService.checkIfSchedulesExist(request.schedules())) {
            throw new NotExistingSchedulesException();
        }

        discordApiWebhooksClient.sendMessage(
                discordApiId,
                token,
                new DiscordMessageRequest(WELCOME_MESSAGE)
        );

        DiscordWebhook discordWebhook = repository.save(
                DiscordWebhook.builder()
                        .discordApiId(discordApiId)
                        .token(token)
                        .addedBy(jwt.getSubject())
                        .schedules(schedules)
                        .build()
        );

        return new DiscordWebhookResponse(
                discordWebhook.getId(),
                discordWebhook.getDiscordApiId(),
                discordWebhook.getToken(),
                discordWebhook.getSchedules()
        );
    }

    private boolean isValidDiscordWebhookUrl(String url) {
        if (!url.startsWith(DISCORD_WEBHOOK_URL_PREFIX)) {
            return false;
        }

        String[] parts = splitUrlIntoParts(url);

        if (parts.length != 2) {
            return false;
        }

        return !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    public String[] splitUrlIntoParts(String url) {
        int beginIdx = DISCORD_WEBHOOK_URL_PREFIX.length();
        String afterPrefix = url.substring(beginIdx);

        return afterPrefix.split("/");
    }

    private String getDiscordApiIdFromUrl(String url) {
        return splitUrlIntoParts(url)[0];
    }

    private String getTokenFromUrl(String url) {
        return splitUrlIntoParts(url)[1];
    }
}
