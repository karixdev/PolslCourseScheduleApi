package com.github.karixdev.courseservice.application.event.handler;

import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent.IgnoredScheduleEventTypeHandlerStrategy;
import com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent.ScheduleEventConcreteTypeHandlerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ScheduleEventHandler implements EventHandler<ScheduleEvent> {

    private final List<ScheduleEventConcreteTypeHandlerStrategy> concreteTypeHandlerStrategies;
    private final IgnoredScheduleEventTypeHandlerStrategy ignoredScheduleEventTypeHandlerStrategy;

    @Override
    public void handle(ScheduleEvent event) {
        ScheduleEventConcreteTypeHandlerStrategy strategy =
                concreteTypeHandlerStrategies.stream()
                        .filter(s -> s.supports(event.type()))
                        .findFirst()
                        .orElse(ignoredScheduleEventTypeHandlerStrategy);

        strategy.handle(event);
    }
}
