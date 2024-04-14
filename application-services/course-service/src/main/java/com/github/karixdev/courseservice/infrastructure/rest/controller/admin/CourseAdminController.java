package com.github.karixdev.courseservice.infrastructure.rest.controller.admin;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.command.DeleteCourseByIdCommand;
import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.command.handler.CommandHandler;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.mapper.ModelMapperWithAttrs;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CommandHandler<CreateCourseCommand> createCourseCommandHandler;
    private final CommandHandler<UpdateCourseByIdCommand> updateCourseByIdCommandHandler;
    private final CommandHandler<DeleteCourseByIdCommand> deleteCourseByIdCommandHandler;

    private final ModelMapper<CourseRequest, CreateCourseCommand> createCourseCommandMapper;
    private final ModelMapperWithAttrs<CourseRequest, UpdateCourseByIdCommand> updateCourseByIdCommandMapper;

    @PostMapping
    ResponseEntity<Void> createCourse(@RequestBody CourseRequest request) {
        CreateCourseCommand command = createCourseCommandMapper.map(request);
        createCourseCommandHandler.handle(command);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> updateCourseById(@RequestBody CourseRequest request, @PathVariable UUID id) {
        UpdateCourseByIdCommand command = updateCourseByIdCommandMapper.map(request, Map.of("id", id));
        updateCourseByIdCommandHandler.handle(command);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCourseById(@PathVariable UUID id) {
        DeleteCourseByIdCommand course = new DeleteCourseByIdCommand(id);
        deleteCourseByIdCommandHandler.handle(course);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
