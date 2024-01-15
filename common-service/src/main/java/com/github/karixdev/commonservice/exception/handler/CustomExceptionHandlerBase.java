package com.github.karixdev.commonservice.exception.handler;

import com.github.karixdev.commonservice.dto.ErrorResponse;
import com.github.karixdev.commonservice.dto.ValidationErrorResponse;
import com.github.karixdev.commonservice.exception.ForbiddenAccessException;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.commonservice.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

public abstract class CustomExceptionHandlerBase {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundExceptionHandler(
            ResourceNotFoundException ex
    ) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            ValidationException ex
    ) {
        Map<String, String> constraints = Map.of(ex.getFieldName(), ex.getMessage());

        return new ResponseEntity<>(
                new ValidationErrorResponse(
                        constraints
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccessException(
            ValidationException ex
    ) {

        return new ResponseEntity<>(
                new ErrorResponse(
                        HttpStatus.FORBIDDEN,
                        ex.getMessage()
                ),
                HttpStatus.FORBIDDEN
        );
    }

}
