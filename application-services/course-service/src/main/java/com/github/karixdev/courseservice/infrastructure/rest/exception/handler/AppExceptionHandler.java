package com.github.karixdev.courseservice.infrastructure.rest.exception.handler;

import com.github.karixdev.courseservice.application.exception.AppException;
import com.github.karixdev.courseservice.infrastructure.rest.exception.payload.ErrorResponse;
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
