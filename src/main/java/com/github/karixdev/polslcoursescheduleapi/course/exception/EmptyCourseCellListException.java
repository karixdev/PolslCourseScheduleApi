package com.github.karixdev.polslcoursescheduleapi.course.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmptyCourseCellListException extends RuntimeException {
    public EmptyCourseCellListException() {
        super("Course cell list is empty");
    }
}
