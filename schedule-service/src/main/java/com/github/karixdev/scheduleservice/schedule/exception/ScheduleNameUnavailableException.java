package com.github.karixdev.scheduleservice.schedule.exception;

import com.github.karixdev.scheduleservice.shared.exception.ValidationException;

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
