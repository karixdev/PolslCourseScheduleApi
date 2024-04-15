package com.github.karixdev.courseservice.infrastructure.rest.controller.user;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.query.handler.QueryHandler;
import com.github.karixdev.courseservice.application.query.user.FindScheduleCoursesInChronologicalOrderQuery;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Publicly available course actions")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final QueryHandler<FindScheduleCoursesInChronologicalOrderQuery, List<PublicCourseDTO>> findScheduleCoursesInChronologicalOrderQueryHandler;

    private final ModelMapper<PublicCourseDTO, PublicCourseResponse> publicCourseResponseModelMapper;

    @Operation(summary = "Gets schedule courses by its id")
    @ApiResponse(
            responseCode = "400",
            description = "Ok",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PublicCourseResponse.class)))
    )
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
