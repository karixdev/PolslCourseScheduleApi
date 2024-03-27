package com.github.karixdev.domainmodelmapperservice.application.exception;

public class EmptyProcessedRawCourseSetException extends RuntimeException {

    public EmptyProcessedRawCourseSetException() {
        super("ProcessedRawCourse set is empty");
    }

}
