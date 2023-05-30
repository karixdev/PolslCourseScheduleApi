package com.example.discordnotificationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnavailableDiscordWebhookException extends RuntimeException {
    public UnavailableDiscordWebhookException() {
        super("Provided Discord webhook is not available");
    }
}
