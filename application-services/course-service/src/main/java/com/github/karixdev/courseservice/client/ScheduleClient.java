package com.github.karixdev.courseservice.client;

import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Optional;
import java.util.UUID;

@HttpExchange("/api/schedules")
public interface ScheduleClient {

    @GetExchange("/{id}")
    Optional<ScheduleResponse> findById(
            @PathVariable(name = "id") UUID id
    );

}
