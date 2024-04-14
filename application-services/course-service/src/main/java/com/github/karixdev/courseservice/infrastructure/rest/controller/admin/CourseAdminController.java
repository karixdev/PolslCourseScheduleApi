package com.github.karixdev.courseservice.infrastructure.rest.controller.admin;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.command.handler.CommandHandler;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CommandHandler<CreateCourseCommand> createCourseCommandHandler;

    private final ModelMapper<CourseRequest, CreateCourseCommand> createCourseCommandMapper;

    @PostMapping
    ResponseEntity<Void> createCourse(@RequestBody CourseRequest request) {
        CreateCourseCommand command = createCourseCommandMapper.map(request);
        createCourseCommandHandler.handle(command);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
