package com.github.karixdev.domaincoursemapperservice.exception;

public class NoScheduleStartTimeException extends RuntimeException {
    public NoScheduleStartTimeException() {
        super("Could not find schedule start time");
    }
}
