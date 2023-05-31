package com.github.karixdev.notificationservice.service;

import com.github.karixdev.notificationservice.client.DiscordWebhookClient;
import com.github.karixdev.notificationservice.dto.DiscordWebhook;
import com.github.karixdev.notificationservice.dto.DiscordWebhookRequest;
import com.github.karixdev.notificationservice.exception.NotValidNotificationMessageException;
import com.github.karixdev.notificationservice.message.NotificationMessage;
import com.github.karixdev.notificationservice.props.NotificationProperties;
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

    public void sendWelcomeMessage(String discordId, String token) {
        DiscordWebhookRequest request = new DiscordWebhookRequest(
                NotificationProperties.WELCOME_MESSAGE
        );

        discordWebhookClient.sendMessage(
                discordId,
                token,
                request
        );
    }
}
