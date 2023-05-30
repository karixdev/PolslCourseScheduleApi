package com.example.discordnotificationservice.service;

import com.example.discordnotificationservice.document.DiscordWebhook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class DiscordWebhookService {
    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";

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

    public String transformDiscordWebhookIntoUrl(DiscordWebhook discordWebhook) {
        return UriComponentsBuilder
                .fromUriString(DISCORD_WEBHOOK_URL_PREFIX)
                .pathSegment(
                        discordWebhook.getDiscordId(),
                        discordWebhook.getToken()
                )
                .toUriString();
    }
}