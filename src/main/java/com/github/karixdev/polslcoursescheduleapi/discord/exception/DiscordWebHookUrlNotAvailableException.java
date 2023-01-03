package com.github.karixdev.polslcoursescheduleapi.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DiscordWebHookUrlNotAvailableException extends RuntimeException {
    public DiscordWebHookUrlNotAvailableException() {
        super("Discord web hook is not available");
    }
}
