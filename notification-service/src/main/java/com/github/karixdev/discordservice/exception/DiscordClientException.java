package com.github.karixdev.discordservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiscordClientException extends RuntimeException {
    public DiscordClientException(HttpStatusCode statusCode) {
        super("ScheduleService responded with %d".formatted(statusCode.value()));
    }
}
