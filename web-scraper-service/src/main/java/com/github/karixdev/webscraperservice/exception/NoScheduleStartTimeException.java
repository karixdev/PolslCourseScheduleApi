package com.github.karixdev.webscraperservice.exception;

public class NoScheduleStartTimeException extends RuntimeException {
    public NoScheduleStartTimeException() {
        super("Could not find schedule start time");
    }
}
