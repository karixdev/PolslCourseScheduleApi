package com.github.karixdev.webscraperservice.course.exception;

public class NoScheduleStartTimeException extends RuntimeException {
    public NoScheduleStartTimeException() {
        super("Could not find schedule start time");
    }
}
