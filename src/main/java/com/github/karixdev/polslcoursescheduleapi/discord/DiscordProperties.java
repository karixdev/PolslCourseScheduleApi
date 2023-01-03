package com.github.karixdev.polslcoursescheduleapi.discord;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class DiscordProperties {
    private final String webhookBaseUrl;

    public DiscordProperties(
            @Value("${discord.webhook-base-url}")
            String webhookBaseUrl
    ) {
        this.webhookBaseUrl = webhookBaseUrl;
    }
}
