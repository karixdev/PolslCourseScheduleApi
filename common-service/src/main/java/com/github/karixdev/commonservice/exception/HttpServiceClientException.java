package com.github.karixdev.commonservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class HttpServiceClientException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Service %s returned client side error status code: %s";

    public HttpServiceClientException(String serviceName, HttpStatusCode statusCode) {
        super(MESSAGE_TEMPLATE.formatted(serviceName, statusCode.toString()));
    }

}
