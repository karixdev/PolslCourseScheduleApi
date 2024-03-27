package com.github.karixdev.domainmodelmapperservice.application.event;

import com.github.karixdev.domainmodelmapperservice.domain.raw.RawSchedule;
import lombok.Builder;

@Builder
public record RawScheduleEvent(String scheduleId, RawSchedule entity) {}
