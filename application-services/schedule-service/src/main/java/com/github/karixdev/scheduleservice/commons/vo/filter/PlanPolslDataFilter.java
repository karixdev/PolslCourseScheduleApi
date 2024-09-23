package com.github.karixdev.scheduleservice.commons.vo.filter;

import lombok.Builder;

import java.util.List;

@Builder
public record PlanPolslDataFilter(
        List<Integer> ids,
        List<Integer> types,
        List<Integer> weedDays
) {}
