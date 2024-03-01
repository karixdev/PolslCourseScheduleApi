package com.github.karixdev.scheduleservice.application.exception;

public class UnavailablePlanPolslIdException extends AppException {

    private static final String MESSAGE_TEMPLATE = "Plan polsl ID %d is unavailable";

    public UnavailablePlanPolslIdException(int planPolslId) {
        super(MESSAGE_TEMPLATE.formatted(planPolslId), 400);
    }

}
