package com.github.karixdev.scheduleservice.infrastructure.rest.exception.handler;

import com.github.karixdev.scheduleservice.application.exception.AppException;
import com.github.karixdev.scheduleservice.infrastructure.rest.exception.handler.payload.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getCode())
                .build();

        return ResponseEntity
                .status(ex.getCode())
                .body(body);
    }

}
