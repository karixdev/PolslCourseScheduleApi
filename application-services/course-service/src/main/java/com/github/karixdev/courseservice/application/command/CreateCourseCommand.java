package com.github.karixdev.courseservice.application.command;

import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;


@Builder
public record CreateCourseCommand(
        UUID scheduleId,
        LocalTime startsAt,
        LocalTime endsAt,
        String name,
        CourseType courseType,
        String teachers,
        DayOfWeek dayOfWeek,
        WeekType weekType,
        String classrooms,
        String additionalInfo
) {}
