package com.github.karixdev.scheduleservice.application.query.user.handler;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.user.FindSchedulesBySemesterAndMajorQuery;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindSchedulesBySemesterAndMajorQueryHandler implements QueryHandler<FindSchedulesBySemesterAndMajorQuery, List<PublicScheduleDTO>> {

    private final ScheduleRepository repository;
    private final ModelMapper<Schedule, PublicScheduleDTO> mapper;

    @Override
    public List<PublicScheduleDTO> handle(com.github.karixdev.scheduleservice.application.query.user.FindSchedulesBySemesterAndMajorQuery query) {
        return repository.findByMajorAndSemester(query.major(), query.semester())
                .stream()
                .map(mapper::map)
                .toList();
    }

}
