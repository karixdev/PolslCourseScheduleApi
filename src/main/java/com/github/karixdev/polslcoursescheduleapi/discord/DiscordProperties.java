package com.github.karixdev.polslcoursescheduleapi.discord;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class DiscordProperties {
    private final String webHookBaseUrl;

    public DiscordProperties(
            @Value("${discord.web-hook-base-url}")
            String webHookBaseUrl
    ) {
        this.webHookBaseUrl = webHookBaseUrl;
    }
}
