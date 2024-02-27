package com.github.karixdev.webscraperservice.domain.exception;

public class EmptyRawCourseSetException extends RuntimeException {

    public EmptyRawCourseSetException() {
        super("RawSchedule cannot contain empty courses set");
    }

}
