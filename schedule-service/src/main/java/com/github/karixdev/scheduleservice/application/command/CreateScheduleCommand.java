package com.github.karixdev.scheduleservice.application.command;

import lombok.Builder;

@Builder
public record CreateScheduleCommand(
        Integer type,
        Integer planPolslId,
        Integer semester,
        String major,
        Integer groupNumber,
        Integer weekDays
) {}
