package com.github.karixdev.scheduleservice.application.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DeleteScheduleByIdCommand(UUID id) {}
