package com.github.karixdev.domainmodelmapperservice.application.event;

import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawSchedule;
import lombok.Builder;

@Builder
public record ProcessedRawScheduleEvent(String scheduleId, ProcessedRawSchedule entity) {}
