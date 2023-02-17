package com.github.karixdev.scheduleservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ScheduleNameUnavailableException extends RuntimeException {
    public ScheduleNameUnavailableException(String name) {
        super(String.format("Schedule name %s is unavailable", name));
    }
}
