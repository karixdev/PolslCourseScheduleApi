package com.github.karixdev.scheduleservice.infrastructure.dal.mapper;

import com.github.karixdev.scheduleservice.application.pagination.PageInfo;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaginationJpaMapper {

    private final ScheduleJpaMapper entityMapper;

    public <T> PageInfo mapPageInfo(Page<T> page) {
        return PageInfo.builder()
                .page(page.getPageable().getPageNumber())
                .size(page.getPageable().getPageSize())
                .numberOfElements(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .build();
    }

    public com.github.karixdev.scheduleservice.application.pagination.Page<Schedule> mapToDomain(Page<ScheduleEntity> jpaPage) {
        PageInfo pageInfo = mapPageInfo(jpaPage);
        List<Schedule> content = jpaPage.getContent()
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();

        return new com.github.karixdev.scheduleservice.application.pagination.Page<>(content, pageInfo);
    }

}
