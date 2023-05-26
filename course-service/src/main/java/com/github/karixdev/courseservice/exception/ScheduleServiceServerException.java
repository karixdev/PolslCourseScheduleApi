package com.github.karixdev.courseservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ScheduleServiceServerException extends RuntimeException {
    public ScheduleServiceServerException(HttpStatusCode statusCode) {
        super("ScheduleService responded with %d".formatted(statusCode.value()));
    }
}
