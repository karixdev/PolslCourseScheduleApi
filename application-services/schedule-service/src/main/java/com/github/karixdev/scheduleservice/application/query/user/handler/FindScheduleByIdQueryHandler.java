package com.github.karixdev.scheduleservice.application.query.user.handler;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.user.FindScheduleByIdQuery;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindScheduleByIdQueryHandler implements QueryHandler<FindScheduleByIdQuery, PublicScheduleDTO> {

    private final ScheduleRepository repository;
    private final ModelMapper<Schedule, PublicScheduleDTO> mapper;

    @Override
    public PublicScheduleDTO handle(FindScheduleByIdQuery query) {
        return repository.findById(query.id())
                .map(mapper::map)
                .orElseThrow(() -> new ScheduleWithIdNotFoundException(query.id()));
    }

}
