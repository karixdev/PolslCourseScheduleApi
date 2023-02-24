package com.github.karixdev.scheduleservice.exception;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class ScheduleNameUnavailableException extends ValidationException {
    public ScheduleNameUnavailableException(String name) {
        super(
                "name",
                String.format(
                        "name %s is unavailable",
                        name
                )
        );
    }
}
