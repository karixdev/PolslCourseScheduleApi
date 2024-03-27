package com.github.karixdev.commonservice.exception.handler;

import com.github.karixdev.commonservice.dto.ErrorResponse;
import com.github.karixdev.commonservice.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class PrefabExceptionHandlerBase {

    private static final String TYPE_MISMATCH_MESSAGE_TEMPLATE = "Provided %s in URL is in invalid format";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> constraintsMap = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .filter(error -> error.getDefaultMessage() != null)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        return new ResponseEntity<>(
                new ValidationErrorResponse(
                        constraintsMap
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        String message = TYPE_MISMATCH_MESSAGE_TEMPLATE.formatted(ex.getName());

        return new ResponseEntity<>(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        message
                ),
                HttpStatus.BAD_REQUEST
        );
    }

}
