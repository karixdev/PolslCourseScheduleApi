package com.github.karixdev.scheduleservice.application.query.user.handler;

import com.github.karixdev.scheduleservice.application.query.user.FindUniqueMajorsQuery;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindUniqueMajorsQueryHandler implements QueryHandler<FindUniqueMajorsQuery, List<String>> {

    private final ScheduleRepository repository;

    @Override
    public List<String> handle(FindUniqueMajorsQuery findUniqueMajorsQuery) {
        return repository.findUniqueMajorsOrderedAlphabetically();
    }

}
