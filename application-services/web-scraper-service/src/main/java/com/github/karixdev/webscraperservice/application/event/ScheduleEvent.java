package com.github.karixdev.webscraperservice.application.event;

import com.github.karixdev.webscraperservice.domain.Schedule;
import lombok.Builder;

@Builder
public record ScheduleEvent(String scheduleId, EventType type, Schedule entity) {}
