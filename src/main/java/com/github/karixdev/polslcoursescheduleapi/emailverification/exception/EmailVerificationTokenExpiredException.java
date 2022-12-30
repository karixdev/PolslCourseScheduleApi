package com.github.karixdev.polslcoursescheduleapi.emailverification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmailVerificationTokenExpiredException extends RuntimeException{
    public EmailVerificationTokenExpiredException() {
        super("Email verification token has expired");
    }
}
