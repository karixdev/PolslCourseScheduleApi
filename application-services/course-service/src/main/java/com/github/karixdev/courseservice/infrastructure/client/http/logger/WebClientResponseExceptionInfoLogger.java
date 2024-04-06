package com.github.karixdev.courseservice.infrastructure.client.http.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

@Slf4j
public class WebClientResponseExceptionInfoLogger {

    public void logException(WebClientResponseException exception) {
        HttpRequest request = exception.getRequest();

        String method = "null";
        String uri = "null";

        if (Objects.nonNull(request)) {
            method = request.getMethod().name();
            uri = request.getURI().toString();
        }

        int statusCode = exception.getStatusCode().value();
        String responseBody = exception.getResponseBodyAsString();

        log.error("Request {} {} failed. Status Code: {}. Response body: {}", method, uri, statusCode, responseBody);
        throw exception;
    }

}
