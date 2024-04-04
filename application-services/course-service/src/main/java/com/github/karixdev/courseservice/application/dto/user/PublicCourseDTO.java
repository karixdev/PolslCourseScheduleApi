package com.github.karixdev.courseservice.application.dto.user;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record PublicCourseDTO(
        UUID id,
        UUID scheduleId,
        String name,
        PublicCourseTypeDTO courseType,
        String teachers,
        String classrooms,
        String additionalInfo,
        DayOfWeek dayOfWeek,
        PublicCourseWeekTypeDTO weekType,
        LocalTime startsAt,
        LocalTime endsAt
) {}
