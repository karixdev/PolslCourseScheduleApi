package com.example.discordnotificationservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    private final String fieldName;

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
