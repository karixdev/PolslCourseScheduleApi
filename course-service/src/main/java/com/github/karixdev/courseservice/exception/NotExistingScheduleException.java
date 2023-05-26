package com.github.karixdev.courseservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotExistingScheduleException extends RuntimeException {
    public NotExistingScheduleException(UUID scheduleId) {
        super("Schedule with id %s does not exist".formatted(scheduleId));
    }
}
