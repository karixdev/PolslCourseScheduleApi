package com.github.karixdev.scheduleservice.application.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateScheduleByIdCommand(
        UUID id,
        Integer type,
        Integer planPolslId,
        Integer semester,
        String major,
        Integer groupNumber,
        Integer weekDays
) {}
