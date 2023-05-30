package com.example.discordnotificationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ScheduleClientException extends RuntimeException {
    public ScheduleClientException() {
        super("Schedule client returned error status code");
    }
}
