package com.github.karixdev.courseservice.infrastructure.rest.controller.user;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.query.handler.QueryHandler;
import com.github.karixdev.courseservice.application.query.user.FindScheduleCoursesInChronologicalOrderQuery;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final QueryHandler<FindScheduleCoursesInChronologicalOrderQuery, List<PublicCourseDTO>> findScheduleCoursesInChronologicalOrderQueryHandler;

    private final ModelMapper<PublicCourseDTO, PublicCourseResponse> publicCourseResponseModelMapper;

    @GetMapping("/schedule/{scheduleId}")
    ResponseEntity<List<PublicCourseResponse>> findScheduleCoursesInChronologicalOrder(@PathVariable UUID scheduleId) {
        FindScheduleCoursesInChronologicalOrderQuery query = new FindScheduleCoursesInChronologicalOrderQuery(scheduleId);
        List<PublicCourseDTO> result = findScheduleCoursesInChronologicalOrderQueryHandler.handle(query);

        return ResponseEntity.ok(
                result.stream()
                        .map(publicCourseResponseModelMapper::map)
                        .toList()
        );
    }

}
