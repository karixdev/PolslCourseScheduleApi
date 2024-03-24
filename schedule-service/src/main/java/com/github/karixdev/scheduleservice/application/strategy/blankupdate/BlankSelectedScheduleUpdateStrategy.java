package com.github.karixdev.scheduleservice.application.strategy.blankupdate;

import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlankSelectedScheduleUpdateStrategy extends BlankSchedulesUpdateStrategy {

    private final ScheduleRepository repository;

    public BlankSelectedScheduleUpdateStrategy(EventProducer<ScheduleEvent> eventProducer, ScheduleRepository repository) {
        super(eventProducer);
        this.repository = repository;
    }

    @Override
    public boolean supports(List<UUID> ids) {
        return !ids.isEmpty();
    }

    @Override
    public void blankUpdate(List<UUID> ids) {
        List<Schedule> schedules = repository.findByIds(ids);
        publishEvents(schedules);
    }
}
