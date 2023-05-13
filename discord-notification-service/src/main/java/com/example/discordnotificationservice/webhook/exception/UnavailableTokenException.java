package com.example.discordnotificationservice.webhook.exception;

import com.example.discordnotificationservice.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnavailableTokenException extends ValidationException {
    public UnavailableTokenException() {
        super("url", "Token in provided url is unavailable");
    }
}