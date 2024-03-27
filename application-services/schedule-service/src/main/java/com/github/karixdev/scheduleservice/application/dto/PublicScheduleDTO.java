package com.github.karixdev.scheduleservice.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PublicScheduleDTO(UUID id, Integer group) {}
