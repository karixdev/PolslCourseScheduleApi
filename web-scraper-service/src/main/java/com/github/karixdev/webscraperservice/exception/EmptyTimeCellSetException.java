package com.github.karixdev.webscraperservice.exception;

public class EmptyTimeCellSetException extends RuntimeException {
    public EmptyTimeCellSetException() {
        super("Time cells set is empty");
    }
}
