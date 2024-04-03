package com.github.karixdev.courseservice.domain.entity.processed;

import lombok.Builder;

import java.util.Set;

@Builder
public record ProcessedRawSchedule(
        Set<ProcessedRawCourse> courses
) {}
