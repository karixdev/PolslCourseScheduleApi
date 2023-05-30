package com.example.discordnotificationservice.exception.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServiceServerException extends RuntimeException {
    public ServiceServerException(HttpStatusCode statusCode) {
        super("Service client returned error status code %d".formatted(statusCode.value()));
    }
}
