package com.github.karixdev.scheduleservice.controller;

import com.github.karixdev.scheduleservice.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService service;

    @PostMapping
    ResponseEntity<ScheduleResponse> create(
            @Valid @RequestBody ScheduleRequest scheduleRequest
    ) {
        return new ResponseEntity<>(
                service.create(scheduleRequest),
                HttpStatus.CREATED
        );
    }
}
