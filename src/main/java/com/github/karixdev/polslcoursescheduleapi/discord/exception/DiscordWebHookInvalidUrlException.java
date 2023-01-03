package com.github.karixdev.polslcoursescheduleapi.discord.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscordWebHookInvalidUrlException extends RuntimeException {
    public DiscordWebHookInvalidUrlException() {
        super("Provided url is not valid");
    }
}
