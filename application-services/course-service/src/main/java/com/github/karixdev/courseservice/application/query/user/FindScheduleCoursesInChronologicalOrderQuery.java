package com.github.karixdev.courseservice.application.query.user;

import java.util.UUID;

public record FindScheduleCoursesInChronologicalOrderQuery(
        UUID scheduleId
) {}
