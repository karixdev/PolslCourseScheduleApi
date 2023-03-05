package com.github.karixdev.webscraperservice.course.domain;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public record Course(
        LocalTime startsAt,
        LocalTime endsAt,
        String name,
        CourseType courseType,
        Set<String> teachers
) {
    public Course(
            LocalTime startsAt,
            LocalTime endsAt,
            String name,
            CourseType courseType
    ) {
        this(
                startsAt,
                endsAt,
                name,
                courseType,
                new HashSet<>()
        );
    }
}
