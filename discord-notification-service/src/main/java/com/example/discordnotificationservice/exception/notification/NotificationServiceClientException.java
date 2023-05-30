package com.example.discordnotificationservice.exception.notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotificationServiceClientException extends RuntimeException {
    public NotificationServiceClientException(HttpStatusCode statusCode) {
        super("Notification client returned error status code %d".formatted(statusCode.value()));
    }
}
