package com.github.karixdev.webscraperservice.exception;

public class EmptyCourseCellsSetException extends RuntimeException {
    public EmptyCourseCellsSetException() {
        super("Course cells set is empty");
    }
}
