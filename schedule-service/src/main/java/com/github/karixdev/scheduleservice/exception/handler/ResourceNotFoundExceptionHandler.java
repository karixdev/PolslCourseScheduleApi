package com.github.karixdev.scheduleservice.exception.handler;

import com.github.karixdev.scheduleservice.dto.ErrorResponse;
import com.github.karixdev.scheduleservice.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceNotFoundExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage()
                ),
                HttpStatus.NOT_FOUND
        );
    }
}
