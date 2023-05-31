package com.github.karixdev.notificationservice.service;

import com.github.karixdev.notificationservice.client.DiscordWebhookClient;
import com.github.karixdev.notificationservice.dto.DiscordWebhook;
import com.github.karixdev.notificationservice.dto.DiscordWebhookRequest;
import com.github.karixdev.notificationservice.exception.NotValidNotificationMessageException;
import com.github.karixdev.notificationservice.message.NotificationMessage;
import com.github.karixdev.notificationservice.props.NotificationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    NotificationService underTest;

    @Mock
    DiscordWebhookClient discordWebhookClient;

    @ParameterizedTest
    @MethodSource("invalidNotificationMessageValues")
    void GivenNotValidNotificationMessage__WhenHandleNotificationMessage_ThenThrowsNotValidNotificationMessageException(NotificationMessage message) {
        // When & Then
        assertThatThrownBy(() -> underTest.handleNotificationMessage(message))
                .isInstanceOf(NotValidNotificationMessageException.class);
    }

    @Test
    void GivenValidNotificationMessage_WhenHandleNotificationMessage_ThenSendsDiscordWebhookMessage() {
        // Given
        NotificationMessage message = new NotificationMessage(
                "content",
                List.of(),
                new DiscordWebhook(
                        "discordId",
                        "token"
                )
        );

        // When
        underTest.handleNotificationMessage(message);

        // Then
        DiscordWebhookRequest expectedRequest = new DiscordWebhookRequest(
                message.content(),
                message.embeds()
        );

        verify(discordWebhookClient).sendMessage(
                eq(message.discordWebhook().discordId()),
                eq(message.discordWebhook().token()),
                eq(expectedRequest)
        );
    }

    @Test
    void GivenDiscordIdAndToken_WhenSendWelcomeMessage_ThenSendsWelcomeMessage() {
        // Given
        String discordId = "discordId";
        String token = "token";

        // When
        underTest.sendWelcomeMessage(discordId, token);

        // Then
        DiscordWebhookRequest expectedRequest = new DiscordWebhookRequest(
                NotificationProperties.WELCOME_MESSAGE
        );

        verify(discordWebhookClient).sendMessage(
                eq(discordId),
                eq(token),
                eq(expectedRequest)
        );
    }

    private static Stream<Arguments> invalidNotificationMessageValues() {
        return Stream.of(
                Arguments.of(new NotificationMessage(
                        "content",
                        List.of(),
                        new DiscordWebhook(
                                null,
                                "token"
                        )
                )),
                Arguments.of(new NotificationMessage(
                        "content",
                        List.of(),
                        new DiscordWebhook(
                                "discordId",
                                null
                        )
                )),
                Arguments.of(new NotificationMessage(
                        null,
                        null,
                        new DiscordWebhook(
                                "discordId",
                                "token"
                        )
                )),
                Arguments.of(new NotificationMessage(
                        null,
                        List.of(),
                        new DiscordWebhook(
                                "discordId",
                                "token"
                        )
                ))
        );
    }
}