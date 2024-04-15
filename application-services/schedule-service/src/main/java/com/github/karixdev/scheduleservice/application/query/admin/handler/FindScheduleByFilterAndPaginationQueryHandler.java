package com.github.karixdev.scheduleservice.application.query.admin.handler;

import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.admin.FindScheduleByFilterAndPaginationQuery;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindScheduleByFilterAndPaginationQueryHandler implements QueryHandler<FindScheduleByFilterAndPaginationQuery, Page<ScheduleDTO>> {

    private final ScheduleRepository repository;
    private final ModelMapper<Schedule, ScheduleDTO> mapper;

    @Override
    public Page<ScheduleDTO> handle(FindScheduleByFilterAndPaginationQuery query) {
        Page<Schedule> domainEntityPage = repository.findByFilterAndPaginate(query.filter(), query.pageRequest());

        List<ScheduleDTO> mappedContent = domainEntityPage.content()
                .stream()
                .map(mapper::map)
                .toList();

        return Page.<ScheduleDTO>builder()
                .pageInfo(domainEntityPage.pageInfo())
                .content(mappedContent)
                .build();
    }

}
