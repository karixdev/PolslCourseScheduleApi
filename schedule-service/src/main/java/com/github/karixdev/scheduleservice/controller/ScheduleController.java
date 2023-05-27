package com.github.karixdev.scheduleservice.controller;

import com.github.karixdev.scheduleservice.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
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

    @GetMapping
    ResponseEntity<List<ScheduleResponse>> findAll(
            @RequestParam(name = "ids", required = false) Set<UUID> ids
    ) {
        return new ResponseEntity<>(
                service.findAll(ids),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    ResponseEntity<ScheduleResponse> findById(
            @PathVariable(name = "id") UUID id
    ) {
        return new ResponseEntity<>(
                service.findById(id),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    ResponseEntity<ScheduleResponse> update(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody ScheduleRequest scheduleRequest
    ) {
        return new ResponseEntity<>(
                service.update(id, scheduleRequest),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ScheduleResponse> delete(
            @PathVariable(name = "id") UUID id
    ) {
        service.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/courses/update")
    ResponseEntity<Void> requestScheduleCoursesUpdate(
            @PathVariable(name = "id") UUID id
    ) {
        service.requestScheduleCoursesUpdate(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
