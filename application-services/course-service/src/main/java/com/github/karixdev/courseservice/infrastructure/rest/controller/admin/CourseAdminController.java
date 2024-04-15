package com.github.karixdev.courseservice.infrastructure.rest.controller.admin;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.command.DeleteCourseByIdCommand;
import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.command.handler.CommandHandler;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.mapper.ModelMapperWithAttrs;
import com.github.karixdev.courseservice.infrastructure.rest.exception.payload.ValidationErrorResponse;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin course actions")
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CommandHandler<CreateCourseCommand> createCourseCommandHandler;
    private final CommandHandler<UpdateCourseByIdCommand> updateCourseByIdCommandHandler;
    private final CommandHandler<DeleteCourseByIdCommand> deleteCourseByIdCommandHandler;

    private final ModelMapper<CourseRequest, CreateCourseCommand> createCourseCommandMapper;
    private final ModelMapperWithAttrs<CourseRequest, UpdateCourseByIdCommand> updateCourseByIdCommandMapper;

    @Operation(summary = "Creates new course")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
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
    ResponseEntity<Void> createCourse(@RequestBody CourseRequest request) {
        CreateCourseCommand command = createCourseCommandMapper.map(request);
        createCourseCommandHandler.handle(command);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Updates whole already existing course by id")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
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
    ResponseEntity<Void> updateCourseById(@RequestBody CourseRequest request, @PathVariable UUID id) {
        UpdateCourseByIdCommand command = updateCourseByIdCommandMapper.map(request, Map.of("id", id));
        updateCourseByIdCommandHandler.handle(command);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Deletes course by id")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
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
    ResponseEntity<Void> deleteCourseById(@PathVariable UUID id) {
        DeleteCourseByIdCommand course = new DeleteCourseByIdCommand(id);
        deleteCourseByIdCommandHandler.handle(course);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
