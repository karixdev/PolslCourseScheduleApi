package com.github.karixdev.courseservice.infrastructure.client.http.decorator;

import com.github.karixdev.courseservice.infrastructure.client.http.HttpInterfacesScheduleServiceClient;
import com.github.karixdev.courseservice.infrastructure.client.http.logger.WebClientResponseExceptionInfoLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class HttpInterfacesScheduleServiceClientResponseExceptionLoggerDecorator implements HttpInterfacesScheduleServiceClient {

    private final HttpInterfacesScheduleServiceClient client;
    private final WebClientResponseExceptionInfoLogger exceptionInfoLogger;

    @Override
    public void findScheduleById(UUID id) {
        try {
            client.findScheduleById(id);
        } catch (WebClientResponseException exception) {
            exceptionInfoLogger.logException(exception);
            throw exception;
        }
    }

}
