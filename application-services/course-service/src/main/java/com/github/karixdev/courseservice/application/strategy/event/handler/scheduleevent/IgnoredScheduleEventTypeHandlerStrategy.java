package com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent;

import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgnoredScheduleEventTypeHandlerStrategy implements ScheduleEventConcreteTypeHandlerStrategy {
    @Override
    public boolean supports(EventType eventType) {
        return false;
    }

    @Override
    public void handle(ScheduleEvent event) {
        log.info("Event {} is ignored due not supported type", event);
    }
}
