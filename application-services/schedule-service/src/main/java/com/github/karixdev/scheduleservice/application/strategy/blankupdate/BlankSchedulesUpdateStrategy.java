package com.github.karixdev.scheduleservice.application.strategy.blankupdate;

import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BlankSchedulesUpdateStrategy {

    private final EventProducer<ScheduleEvent> eventProducer;

    public abstract boolean supports(List<UUID> ids);
    public abstract void blankUpdate(List<UUID> ids);

    protected void publishEvents(List<Schedule> schedules) {
        schedules.forEach(schedule -> {
            ScheduleEvent event = ScheduleEvent.builder()
                    .type(EventType.CREATE)
                    .scheduleId(schedule.getId().toString())
                    .entity(schedule)
                    .build();

            eventProducer.produce(event);
        });
    }

}
