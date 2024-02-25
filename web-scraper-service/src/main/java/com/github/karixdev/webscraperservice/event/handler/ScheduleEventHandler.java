package com.github.karixdev.webscraperservice.event.handler;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.webscraperservice.event.producer.EventProducer;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.service.PlanPolslService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleEventHandler implements EventHandler<ScheduleEvent> {

    private final PlanPolslService planPolslService;
    private final EventProducer<ScheduleRaw> producer;

    private static final List<EventType> SUPPORTED_EVENT_TYPES = List.of(EventType.CREATE, EventType.UPDATE);

    @Override
    public void handle(ScheduleEvent event) {
        if (!SUPPORTED_EVENT_TYPES.contains(event.eventType())) {
            log.info("Ignoring event {} because it has unsupported event type", event);
            return;
        }

        PlanPolslResponse planPolslResponse =
                planPolslService.getSchedule(event.planPolslId(), event.type(), event.wd());

        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(event.scheduleId())
                .timeCells(planPolslResponse.timeCells())
                .courseCells(planPolslResponse.courseCells())
                .build();

        producer.produce(scheduleRaw);
    }
}
