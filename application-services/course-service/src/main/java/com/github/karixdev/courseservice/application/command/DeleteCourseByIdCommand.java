package com.github.karixdev.courseservice.application.command;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DeleteCourseByIdCommand(UUID id) {}
