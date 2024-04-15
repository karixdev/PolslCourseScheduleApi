package com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent;

import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ScheduleEventDeleteTypeHandlerStrategy implements ScheduleEventConcreteTypeHandlerStrategy {

    private final CourseRepository repository;
    private final TransactionManager transactionManager;

    @Override
    public boolean supports(EventType eventType) {
        return Objects.equals(eventType, EventType.DELETE);
    }

    @Override
    public void handle(ScheduleEvent event) {
        transactionManager.execute(() -> repository.deleteByScheduleId(event.entity().id()));
    }
}
