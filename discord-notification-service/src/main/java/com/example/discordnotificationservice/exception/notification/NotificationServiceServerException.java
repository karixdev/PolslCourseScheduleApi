package com.example.discordnotificationservice.exception.notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NotificationServiceServerException extends RuntimeException {
    public NotificationServiceServerException(HttpStatusCode statusCode) {
        super("Notification client returned error status code %d".formatted(statusCode.value()));
    }
}
