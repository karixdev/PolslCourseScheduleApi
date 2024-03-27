package com.github.karixdev.webscraperservice.application.event;

import com.github.karixdev.webscraperservice.domain.RawSchedule;
import lombok.Builder;

@Builder
public record RawScheduleEvent(String scheduleId, RawSchedule entity) {}
