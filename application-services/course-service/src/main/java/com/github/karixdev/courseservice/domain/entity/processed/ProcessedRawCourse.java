package com.github.karixdev.courseservice.domain.entity.processed;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record ProcessedRawCourse(
    UUID scheduleId,
    String name,
    ProcessedRawCourseType courseType,
    String teachers,
    String classrooms,
    String additionalInfo,
    DayOfWeek dayOfWeek,
    ProcessedRawCourseWeekType weekType,
    LocalTime startsAt,
    LocalTime endsAt
) {}
