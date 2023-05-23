package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.Embedded;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
    private final DiscordWebhookClient client;

    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";
    private static final String WELCOME_MESSAGE = "Hello form PolslCourseApi!";
    public static final int EMBED_COLOR = 10360031;
    public static final String SCHEDULE_UPDATE_DESCRIPTION = "Schedule %s has been updated";
    public static final String SCHEDULE_UPDATE_TITLE = "Schedule update";

    public boolean isNotValidDiscordWebhookUrl(String url) {
        if (!url.startsWith(DISCORD_WEBHOOK_URL_PREFIX)) {
            return true;
        }

        String[] parts = splitUrlIntoParts(url);

        if (parts.length != 2) {
            return true;
        }

        return parts[0].isEmpty() || parts[1].isEmpty();
    }

    public DiscordWebhook getDiscordWebhookFromUrl(String url) {
        String[] parts = splitUrlIntoParts(url);

        return new DiscordWebhook(
                parts[0],
                parts[1]
        );
    }

    private String[] splitUrlIntoParts(String url) {
        int beginIdx = DISCORD_WEBHOOK_URL_PREFIX.length();
        String afterPrefix = url.substring(beginIdx);

        return afterPrefix.split("/");
    }

    public void sendWelcomeMessage(DiscordWebhook discordWebhook) {
        client.sendMessage(
                discordWebhook.getDiscordId(),
                discordWebhook.getToken(),
                new DiscordWebhookRequest(WELCOME_MESSAGE)
        );
    }

    public String transformDiscordWebhookIntoUrl(DiscordWebhook discordWebhook) {
        return UriComponentsBuilder
                .fromUriString(DISCORD_WEBHOOK_URL_PREFIX)
                .pathSegment(
                        discordWebhook.getDiscordId(),
                        discordWebhook.getToken()
                )
                .toUriString();
    }

    public void sendScheduleUpdateNotification(DiscordWebhook discordWebhook, String scheduleName) {
        Embedded embedded = new Embedded(
                SCHEDULE_UPDATE_TITLE,
                SCHEDULE_UPDATE_DESCRIPTION.formatted(scheduleName),
                EMBED_COLOR
        );
        DiscordWebhookRequest request = new DiscordWebhookRequest(embedded);

        client.sendMessage(
                discordWebhook.getDiscordId(),
                discordWebhook.getToken(),
                request
        );
    }
}