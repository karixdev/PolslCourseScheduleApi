package com.github.karixdev.scheduleservice.application.exception;

public class UnavailableScheduleNameException extends AppException {

    private static final String MESSAGE_TEMPLATE = "Schedule name %s is unavailable";

    public UnavailableScheduleNameException(String name) {
        super(MESSAGE_TEMPLATE.formatted(name), 400);
    }

}
