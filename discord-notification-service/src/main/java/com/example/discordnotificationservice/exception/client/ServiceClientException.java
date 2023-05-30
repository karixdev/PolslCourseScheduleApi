package com.example.discordnotificationservice.exception.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ServiceClientException extends RuntimeException {
    public ServiceClientException(HttpStatusCode statusCode) {
        super("Service client returned error status code %d".formatted(statusCode.value()));
    }
}
