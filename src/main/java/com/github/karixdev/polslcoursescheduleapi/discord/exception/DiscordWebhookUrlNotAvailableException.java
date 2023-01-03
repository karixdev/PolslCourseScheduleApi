package com.github.karixdev.polslcoursescheduleapi.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DiscordWebhookUrlNotAvailableException extends RuntimeException {
    public DiscordWebhookUrlNotAvailableException() {
        super("Discord web hook is not available");
    }
}
