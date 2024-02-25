package com.github.karixdev.webscraperservice.exception;

public class EmptyCourseCellsSetException extends RuntimeException {

    public EmptyCourseCellsSetException(int scheduleId) {
        super("Schedule %s has empty course cells set".formatted(scheduleId));
    }

}
