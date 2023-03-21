package com.github.karixdev.webscraperservice.planpolsl.exception;

public class EmptyCourseCellsSetException extends RuntimeException {
    public EmptyCourseCellsSetException() {
        super("Course cells set is empty");
    }
}
