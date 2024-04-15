package com.github.karixdev.courseservice.domain.entity.processed;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record ProcessedRawTimeInterval(LocalTime start, LocalTime end) {}
