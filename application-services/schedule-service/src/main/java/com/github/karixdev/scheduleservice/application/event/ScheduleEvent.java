package com.github.karixdev.scheduleservice.application.event;

import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import lombok.Builder;

@Builder
public record ScheduleEvent(
        EventType type,
        String scheduleId,
        Schedule entity
) {}
