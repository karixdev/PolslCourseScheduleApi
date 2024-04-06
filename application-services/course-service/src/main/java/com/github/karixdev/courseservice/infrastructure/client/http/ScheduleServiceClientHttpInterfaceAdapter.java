package com.github.karixdev.courseservice.infrastructure.client.http;

import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduleServiceClientHttpInterfaceAdapter implements ScheduleServiceClient {

    private final HttpInterfacesScheduleServiceClient client;

    @Override
    public Boolean doesScheduleWithIdExist(UUID id) {
        try {
            client.findScheduleById(id);
            return true;
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return false;
            }

            throw exception;
        }
    }
}
