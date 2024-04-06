package com.github.karixdev.courseservice.infrastructure.client.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;

public interface HttpInterfacesScheduleServiceClient {

    @GetExchange("/api/queries/schedules/{id}")
    void findScheduleById(@PathVariable("id") UUID id);

}
