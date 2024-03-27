package com.github.karixdev.webscraperservice.domain.exception;

public class EmptyRawTimeIntervalSetException extends RuntimeException {

    public EmptyRawTimeIntervalSetException() {
        super("RawSchedule cannot contain empty timeIntervals set");
    }

}
