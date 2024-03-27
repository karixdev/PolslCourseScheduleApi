package com.github.karixdev.scheduleservice.application.query.admin;

import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import lombok.Builder;

@Builder
public record FindScheduleByFilterAndPaginationQuery(
        ScheduleFilter filter,
        PageRequest pageRequest
) {}
