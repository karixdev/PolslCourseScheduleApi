package com.github.karixdev.scheduleservice.application.query.user.handler;

import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.user.FindSemestersByMajorQuery;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindSemestersByMajorQueryHandler implements QueryHandler<FindSemestersByMajorQuery, List<Integer>> {

    private final ScheduleRepository repository;

    @Override
    public List<Integer> handle(FindSemestersByMajorQuery query) {
        return repository.findSemestersByMajorOrderAsc(query.major());
    }

}
