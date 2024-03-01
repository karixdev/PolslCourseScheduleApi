package com.github.karixdev.scheduleservice.infrastructure.rest.controller.query.user;

import com.github.karixdev.scheduleservice.application.query.user.FindUniqueMajorsQuery;
import com.github.karixdev.scheduleservice.application.query.user.handler.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/queries/schedules")
@RequiredArgsConstructor
public class UserScheduleQueryController {

    private final QueryHandler<FindUniqueMajorsQuery, List<String>> findUniqueMajorsQueryHandler;

    @GetMapping("/majors")
    ResponseEntity<List<String>> majors() {
        FindUniqueMajorsQuery query = new FindUniqueMajorsQuery();
        List<String> majors = findUniqueMajorsQueryHandler.handle(query);

        return ResponseEntity.ok(majors);
    }

}
