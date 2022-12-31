package com.github.karixdev.polslcoursescheduleapi.schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleNameNotAvailableException extends RuntimeException {
    public ScheduleNameNotAvailableException() {
        super("Schedule name is not available");
    }
}
