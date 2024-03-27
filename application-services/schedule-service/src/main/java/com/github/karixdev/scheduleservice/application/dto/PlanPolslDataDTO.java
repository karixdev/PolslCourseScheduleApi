package com.github.karixdev.scheduleservice.application.dto;

import lombok.Builder;

@Builder
public record PlanPolslDataDTO(
        Integer id,
        Integer type,
        Integer weekDays
) {}
