package com.github.karixdev.courseservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ScheduleServiceClientException extends RuntimeException {
    public ScheduleServiceClientException(HttpStatusCode statusCode) {
        super("ScheduleService responded with %d".formatted(statusCode.value()));
    }
}
