package com.github.karixdev.discordservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class NotValidNotificationMessageException extends RuntimeException {
    public NotValidNotificationMessageException() {
        super("Provided notification message is not valid");
    }
}
