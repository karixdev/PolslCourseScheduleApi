package com.github.karixdev.domainmodelmapperservice.domain.processed;

import lombok.Builder;

import java.util.Set;

@Builder
public record ProcessedRawSchedule(
        Set<ProcessedRawCourse> courses
) {}
