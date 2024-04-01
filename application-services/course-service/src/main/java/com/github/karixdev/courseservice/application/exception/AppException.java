package com.github.karixdev.courseservice.application.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final int code;

    public AppException(String message, int code) {
        super(message);
        this.code = code;
    }

}
