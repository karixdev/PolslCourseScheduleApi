package com.github.karixdev.scheduleservice.application.query.admin;

import com.github.karixdev.scheduleservice.commons.vo.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.commons.vo.pagination.PageRequest;
import lombok.Builder;

@Builder
public record FindScheduleByFilterAndPaginationQuery(
        ScheduleFilter filter,
        PageRequest pageRequest
) {}
