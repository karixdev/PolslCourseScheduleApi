package com.github.karixdev.scheduleservice.infrastructure.rest.controller.params;

import java.util.List;
import java.util.UUID;

public record ScheduleFilterParams(
        List<UUID> id,
        List<String> major,
        List<Integer> semester,
        List<Integer> group
) {}
