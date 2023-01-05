package com.github.karixdev.polslcoursescheduleapi.planpolsl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PlanPolslWebClientException extends RuntimeException{
    public PlanPolslWebClientException() {
        super("Connection to plan.pols.pl failed");
    }
}
