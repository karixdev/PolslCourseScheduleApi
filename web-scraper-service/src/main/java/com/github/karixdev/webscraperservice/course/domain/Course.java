package com.github.karixdev.webscraperservice.course.domain;

import java.time.LocalTime;

public record Course(
        LocalTime startsAt,
        LocalTime endsAt,
        String name,
        CourseType courseType
) {}
