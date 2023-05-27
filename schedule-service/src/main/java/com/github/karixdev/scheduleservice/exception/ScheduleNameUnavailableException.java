package com.github.karixdev.scheduleservice.exception;

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
