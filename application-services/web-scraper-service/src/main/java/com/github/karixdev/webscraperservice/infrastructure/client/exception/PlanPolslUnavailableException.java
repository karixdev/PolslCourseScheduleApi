package com.github.karixdev.webscraperservice.infrastructure.client.exception;

import lombok.Getter;

@Getter
public class PlanPolslUnavailableException extends RuntimeException {

    public PlanPolslUnavailableException(int statusCode) {
        super("plan.polsl.pl responded with error status code %d".formatted(statusCode));
    }

}