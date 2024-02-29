package com.github.karixdev.scheduleservice.application.command;

import lombok.Builder;

@Builder
public record CreateScheduleCommand(
        Integer type,
        Integer planPolslId,
        Integer semester,
        String name,
        Integer groupNumber,
        Integer weekDays
) {}
