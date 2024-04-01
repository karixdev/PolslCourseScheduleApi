package com.github.karixdev.courseservice.application.exception;

import java.util.UUID;

public class CourseWithIdNotFoundException extends AppException {

    private static final String MESSAGE_TEMPLATE = "Course with id %s not found";

    public CourseWithIdNotFoundException(UUID id) {
        super(MESSAGE_TEMPLATE.formatted(id), 404);
    }

}
