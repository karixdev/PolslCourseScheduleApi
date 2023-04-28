package com.example.discordnotificationservice.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscordApiException extends RuntimeException {
    public DiscordApiException() {
        super("Discord api returned error status");
    }
}