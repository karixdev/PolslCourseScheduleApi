package com.github.karixdev.polslcoursescheduleapi.email.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SendingEmailException extends RuntimeException {
    public SendingEmailException() {
        super("Error occurred while sending email");
    }
}
