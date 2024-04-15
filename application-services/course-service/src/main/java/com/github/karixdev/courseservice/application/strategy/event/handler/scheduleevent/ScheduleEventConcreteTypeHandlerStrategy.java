package com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent;

import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;

public interface ScheduleEventConcreteTypeHandlerStrategy {
    boolean supports(EventType eventType);
    void handle(ScheduleEvent event);
}
