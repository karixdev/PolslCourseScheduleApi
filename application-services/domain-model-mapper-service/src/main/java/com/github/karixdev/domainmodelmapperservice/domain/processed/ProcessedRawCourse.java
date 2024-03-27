package com.github.karixdev.domainmodelmapperservice.domain.processed;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record ProcessedRawCourse(
    UUID scheduleId,
    String name,
    CourseType courseType,
    String teachers,
    String classroom,
    String additionalInfo,
    DayOfWeek dayOfWeek,
    WeekType weekType,
    LocalTime startsAt,
    LocalTime endsAt
) {}
