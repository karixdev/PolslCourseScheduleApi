package com.github.karixdev.webscraperservice.planpolsl.exception;

import lombok.Getter;

@Getter
public class PlanPolslUnavailableException extends RuntimeException {
    private final int planPolslId;
    private final int type;
    private final int wd;

    public PlanPolslUnavailableException(String message, int planPolslId, int type, int wd) {
        super(message);

        this.planPolslId = planPolslId;
        this.type = type;
        this.wd = wd;
    }
}