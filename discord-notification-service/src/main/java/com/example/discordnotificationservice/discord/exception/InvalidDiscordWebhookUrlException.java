package com.example.discordnotificationservice.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDiscordWebhookUrlException extends RuntimeException {
    public InvalidDiscordWebhookUrlException() {
        super("Provided Discord webhook url is invalid");
    }
}
