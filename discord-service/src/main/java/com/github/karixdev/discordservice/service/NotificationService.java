package com.github.karixdev.discordservice.service;

import com.github.karixdev.discordservice.client.DiscordWebhookClient;
import com.github.karixdev.discordservice.dto.DiscordWebhook;
import com.github.karixdev.discordservice.dto.DiscordWebhookRequest;
import com.github.karixdev.discordservice.exception.NotValidNotificationMessageException;
import com.github.karixdev.discordservice.message.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final DiscordWebhookClient discordWebhookClient;

    public void handleNotificationMessage(NotificationMessage message) {
        if (!isNotificationMessageValid(message)) {
            throw new NotValidNotificationMessageException();
        }

        DiscordWebhookRequest request = new DiscordWebhookRequest(
                message.content(),
                message.embeds()
        );

        discordWebhookClient.sendMessage(
                message.discordWebhook().discordId(),
                message.discordWebhook().token(),
                request
        );
    }

    private boolean isNotificationMessageValid(NotificationMessage message) {
        DiscordWebhook discordWebhook = message.discordWebhook();

        if (discordWebhook.discordId() == null
            || discordWebhook.token() == null
        ) {
            return false;
        }

        return message.content() != null || (message.embeds() != null && !message.embeds().isEmpty());
    }
}
