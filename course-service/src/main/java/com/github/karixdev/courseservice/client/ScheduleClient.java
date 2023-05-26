package com.github.karixdev.courseservice.client;

import com.github.karixdev.courseservice.dto.ScheduleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange("/api/schedules")
public interface ScheduleClient {
    @GetExchange("/{id}")
    ResponseEntity<ScheduleResponse> findById(
            @PathVariable(name = "id") UUID id
    );
}
