package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.dto.CourseRequest;
import com.github.karixdev.scheduleservice.course.dto.CourseResponse;
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
            @Valid @RequestBody CourseRequest courseRequest,
            @PathVariable UUID id
    ) {
        return new ResponseEntity<>(
                service.update(id, courseRequest),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        service.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
