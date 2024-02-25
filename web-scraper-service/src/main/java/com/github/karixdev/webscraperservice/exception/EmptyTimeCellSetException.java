package com.github.karixdev.webscraperservice.exception;

public class EmptyTimeCellSetException extends RuntimeException {

    public EmptyTimeCellSetException(int scheduleId) {
        super("Schedule %s has empty time cells set".formatted(scheduleId));
    }

}
