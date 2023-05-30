package com.example.discordnotificationservice.exception.handler;

import com.example.discordnotificationservice.dto.ValidationErrorResponse;
import com.example.discordnotificationservice.exception.ValidationException;
import com.example.discordnotificationservice.converter.NameCaseConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> constraintsMap = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(error -> error.getDefaultMessage() != null)
                .collect(Collectors.toMap(
                        error -> NameCaseConverter.camelToSnake(error.getField()),
                        FieldError::getDefaultMessage
                ));

        return new ResponseEntity<>(
                new ValidationErrorResponse(
                        constraintsMap
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            ValidationException exception
    ) {
        Map<String, String> constraints = Map.of(
                exception.getFieldName(), exception.getMessage()
        );

        return new ResponseEntity<>(
                new ValidationErrorResponse(
                    constraints
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}
