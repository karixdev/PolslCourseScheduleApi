package com.example.discordnotificationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException() {
        super("Access to this resource forbidden");
    }
}
