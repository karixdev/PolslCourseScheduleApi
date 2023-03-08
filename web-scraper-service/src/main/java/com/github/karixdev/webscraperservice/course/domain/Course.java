package com.github.karixdev.webscraperservice.course.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public record Course(
        LocalTime startsAt,
        LocalTime endsAt,
        String name,
        CourseType courseType,
        Set<String> teachers,
        DayOfWeek dayOfWeek,
        Weeks weeks,
        Set<String> rooms
) {
    public Course(
            LocalTime startsAt,
            LocalTime endsAt,
            String name,
            CourseType courseType,
            DayOfWeek dayOfWeek,
            Weeks weeks
    ) {
        this(
                startsAt,
                endsAt,
                name,
                courseType,
                new HashSet<>(),
                dayOfWeek,
                weeks,
                new HashSet<>()
        );
    }
}
