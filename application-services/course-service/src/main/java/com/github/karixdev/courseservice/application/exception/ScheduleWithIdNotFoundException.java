package com.github.karixdev.courseservice.application.exception;

import java.util.UUID;

public class ScheduleWithIdNotFoundException extends AppException {

    private static final String MESSAGE_TEMPLATE = "Schedule with %s not found";

    public ScheduleWithIdNotFoundException(UUID id) {
        super(MESSAGE_TEMPLATE.formatted(id), 400);
    }

}
