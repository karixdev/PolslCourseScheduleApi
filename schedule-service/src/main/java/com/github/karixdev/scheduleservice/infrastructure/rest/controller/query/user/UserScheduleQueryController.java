package com.github.karixdev.scheduleservice.infrastructure.rest.controller.query.user;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.application.query.user.FindSchedulesBySemesterAndMajorQuery;
import com.github.karixdev.scheduleservice.application.query.user.FindSemestersByMajorQuery;
import com.github.karixdev.scheduleservice.application.query.user.FindUniqueMajorsQuery;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.PublicScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/queries/schedules")
@RequiredArgsConstructor
public class UserScheduleQueryController {

    private final QueryHandler<FindUniqueMajorsQuery, List<String>> findUniqueMajorsQueryHandler;
    private final QueryHandler<FindSemestersByMajorQuery, List<Integer>> findSemestersByMajorQueryHandler;
    private final QueryHandler<FindSchedulesBySemesterAndMajorQuery, List<PublicScheduleDTO>> findSchedulesBySemesterAndMajorQueryHandler;

    @GetMapping("/majors")
    ResponseEntity<List<String>> majors() {
        FindUniqueMajorsQuery query = new FindUniqueMajorsQuery();
        List<String> majors = findUniqueMajorsQueryHandler.handle(query);

        return ResponseEntity.ok(majors);
    }

    @GetMapping("/majors/{major}/semesters")
    ResponseEntity<List<Integer>> semestersByMajor(@PathVariable String major) {
        FindSemestersByMajorQuery query = new FindSemestersByMajorQuery(major);
        List<Integer> semesters = findSemestersByMajorQueryHandler.handle(query);

        return ResponseEntity.ok(semesters);
    }

    @GetMapping("/majors/{major}/semesters/{semester}")
    ResponseEntity<List<PublicScheduleResponse>> schedulesByMajorAndSemester(
            @PathVariable String major,
            @PathVariable Integer semester
    ) {
        FindSchedulesBySemesterAndMajorQuery query = new FindSchedulesBySemesterAndMajorQuery(major, semester);
        List<PublicScheduleDTO> schedules = findSchedulesBySemesterAndMajorQueryHandler.handle(query);

        List<PublicScheduleResponse> result = schedules.stream()
                .map(schedule ->
                        PublicScheduleResponse.builder()
                                .id(schedule.id())
                                .group(schedule.group())
                                .build()
                )
                .toList();

        return ResponseEntity.ok(result);
    }

}