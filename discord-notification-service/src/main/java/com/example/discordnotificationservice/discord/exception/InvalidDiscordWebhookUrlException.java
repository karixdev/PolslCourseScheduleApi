package com.example.discordnotificationservice.discord.exception;

import com.example.discordnotificationservice.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDiscordWebhookUrlException extends ValidationException {
    public InvalidDiscordWebhookUrlException() {
        super("url", "Provided Discord webhook url is invalid");
    }
}
