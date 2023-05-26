package com.github.karixdev.courseservice.controller;

import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService service;

    @PostMapping
    ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CourseRequest courseRequest
    ) {
        return new ResponseEntity<>(
                service.create(courseRequest),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    ResponseEntity<CourseResponse> update(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody CourseRequest courseRequest
    ) {
        return new ResponseEntity<>(
                service.update(id, courseRequest),
                HttpStatus.OK
        );
    }
}
