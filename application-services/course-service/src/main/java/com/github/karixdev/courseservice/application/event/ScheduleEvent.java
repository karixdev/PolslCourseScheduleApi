package com.github.karixdev.courseservice.application.event;

import com.github.karixdev.courseservice.domain.entity.schedule.Schedule;
import lombok.Builder;

@Builder
public record ScheduleEvent(String scheduleId, EventType type, Schedule entity) {}
