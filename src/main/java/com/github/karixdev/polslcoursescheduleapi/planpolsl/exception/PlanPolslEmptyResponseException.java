package com.github.karixdev.polslcoursescheduleapi.planpolsl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PlanPolslEmptyResponseException extends RuntimeException {
    public PlanPolslEmptyResponseException() {
        super("plan.polsl.pl returned empty response");
    }
}
