package com.github.karixdev.scheduleservice.application.exception;

public class NoBlankScheduleUpdateStrategyException extends AppException {

    public NoBlankScheduleUpdateStrategyException() {
        super("No blank schedule update strategy found", 500);
    }

}
