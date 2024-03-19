package com.github.karixdev.scheduleservice.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ScheduleDTO(
        UUID id,
        String major,
        Integer semester,
        Integer groupNumber,
        PlanPolslDataDTO planPolslData
) {}
