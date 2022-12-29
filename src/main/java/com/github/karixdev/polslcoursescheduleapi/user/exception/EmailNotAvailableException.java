package com.github.karixdev.polslcoursescheduleapi.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailNotAvailableException extends RuntimeException {
    public EmailNotAvailableException() {
        super("Email is not available");
    }
}
