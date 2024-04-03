package com.github.karixdev.courseservice.application.event;

import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawSchedule;
import lombok.Builder;

@Builder
public record ProcessedRawScheduleEvent(String scheduleId, ProcessedRawSchedule entity) {}
