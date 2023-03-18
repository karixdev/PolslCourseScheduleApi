package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.schedule.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    ResponseEntity<List<ScheduleResponse>> findAll() {
        return new ResponseEntity<>(
                service.findAll(),
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
    public ResponseEntity<Void> requestScheduleCoursesUpdate(
            @PathVariable(name = "id") UUID id
    ) {
        service.requestScheduleCoursesUpdate(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
