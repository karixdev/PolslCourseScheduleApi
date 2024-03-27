package com.github.karixdev.courseservice.controller;

import com.github.karixdev.commonservice.docs.schema.ErrorResponseSchema;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.service.CourseService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course controller", description = "All actions except GET are forbidden to normal user")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService service;

    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(schema = @Schema(implementation = CourseResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
    @PostMapping
    ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CourseRequest courseRequest
    ) {
        return new ResponseEntity<>(
                service.create(courseRequest),
                HttpStatus.CREATED
        );
    }

    @ApiResponse(
            responseCode = "200",
            description = "OK. Values are sorted in chronological order",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseResponse.class)))
    )
    @GetMapping("/schedule/{scheduleId}")
    ResponseEntity<List<CourseResponse>> findCoursesBySchedule(
            @PathVariable(name = "scheduleId") UUID scheduleId
    ) {
        return new ResponseEntity<>(
                service.findCoursesBySchedule(scheduleId),
                HttpStatus.OK
        );
    }

    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = CourseResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
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

    @ApiResponse(
            responseCode = "204",
            description = "No content",
            content = @Content(schema = @Schema(implementation = CourseResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
    @DeleteMapping("/{id}")
    ResponseEntity<CourseResponse> update(
            @PathVariable(name = "id") UUID id
    ) {
        service.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
