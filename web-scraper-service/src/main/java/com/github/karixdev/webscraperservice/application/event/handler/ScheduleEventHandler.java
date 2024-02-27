package com.github.karixdev.webscraperservice.application.event.handler;

import com.github.karixdev.webscraperservice.application.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.application.event.EventType;
import com.github.karixdev.webscraperservice.application.event.RawScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.ScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.producer.EventProducer;
import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import com.github.karixdev.webscraperservice.application.props.PlanPolslClientProperties;
import com.github.karixdev.webscraperservice.application.scraper.PlanPolslResponseContentScraper;
import com.github.karixdev.webscraperservice.domain.RawSchedule;
import com.github.karixdev.webscraperservice.domain.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleEventHandler implements EventHandler<ScheduleEvent> {

    private final PlanPolslClient planPolslClient;
    private final PlanPolslResponseContentScraper scraper;
    private final EventProducer<RawScheduleEvent> producer;

    private static final List<EventType> SUPPORTED_EVENT_TYPES = List.of(EventType.CREATE, EventType.UPDATE);

    @Override
    public void handle(ScheduleEvent event) {
        if (!SUPPORTED_EVENT_TYPES.contains(event.type())) {
            log.info("Ignoring event {} because it has unsupported event type", event);
            return;
        }

        Schedule schedule = event.entity();
        PlanPolslResponse planPolslResponse = planPolslClient.getSchedule(
                schedule.planPolslId(),
                schedule.type(),
                schedule.wd(),
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H
        );

        RawSchedule rawSchedule = scraper.scrapSchedule(planPolslResponse);

        RawScheduleEvent rawScheduleEvent = RawScheduleEvent.builder()
                .scheduleId(event.scheduleId())
                .entity(rawSchedule)
                .build();

        producer.produce(rawScheduleEvent);
    }
}
