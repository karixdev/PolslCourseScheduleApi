package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.exception.EmptyTimeCellSetException;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.producer.RawCourseProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final PlanPolslService planPolslService;
    private final RawCourseProducer producer;

    private static final EventType[] SUPPORTED_EVENT_TYPES = {EventType.CREATE, EventType.UPDATE};

    public void handleScheduleEvent(ConsumerRecord<String, ScheduleEvent> consumerRecord) {
        ScheduleEvent value = consumerRecord.value();

        if (!Arrays.asList(SUPPORTED_EVENT_TYPES).contains(value.eventType())) {
            log.info("Event {} has not supported type {}", consumerRecord, value.eventType());
            return;
        }

        getScheduleFromPlanPolsl(value);
    }

    private void getScheduleFromPlanPolsl(ScheduleEvent scheduleEvent) {
        PlanPolslResponse planPolslResponse = planPolslService.getSchedule(
                scheduleEvent.planPolslId(),
                scheduleEvent.type(),
                scheduleEvent.wd()
        );

        if (planPolslResponse.courseCells().isEmpty()) {
            throw new EmptyCourseCellsSetException(scheduleEvent.scheduleId());
        }

        if (planPolslResponse.timeCells().isEmpty()) {
            throw new EmptyTimeCellSetException(scheduleEvent.scheduleId());
        }

        producer.produceRawCourse(
                scheduleEvent.scheduleId(),
                planPolslResponse.courseCells(),
                planPolslResponse.timeCells()
        );
    }

}
