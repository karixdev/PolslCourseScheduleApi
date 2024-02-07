package com.github.karixdev.commonservice.event.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.commonservice.event.EventType;
import lombok.Builder;

@Builder
public record ScheduleEvent(
        @JsonProperty("eventType")
        EventType eventType,
        @JsonProperty("scheduleId")
        String scheduleId,
        @JsonProperty("type")
        Integer type,
        @JsonProperty("planPolslId")
        Integer planPolslId,
        @JsonProperty("wd")
        Integer wd
) {
}
