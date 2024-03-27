package com.github.karixdev.scheduleservice.application.filter;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ScheduleFilter(
        PlanPolslDataFilter planPolslDataFilter,
        List<UUID> ids,
        List<String> majors,
        List<Integer> semesters,
        List<Integer> groups
) {}
