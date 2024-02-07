package com.github.karixdev.commonservice.event.course;

import lombok.Builder;

import java.util.Collection;

@Builder
public record ScheduleCoursesEvent(
        String scheduleId,
        Collection<String> created,
        Collection<String> updated,
        Collection<String> deleted
) {}
