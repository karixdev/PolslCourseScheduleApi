package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.exception.EmptyTimeCellSetException;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.message.ScheduleEventMessage;
import com.github.karixdev.webscraperservice.producer.RawCoursesProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final PlanPolslService planPolslService;
    private final RawCoursesProducer producer;

    public void handleScheduleCreateAndUpdate(ScheduleEventMessage message) {
        PlanPolslResponse planPolslResponse = planPolslService.getSchedule(
                message.planPolslId(),
                message.type(),
                message.wd()
        );

        if (planPolslResponse.courseCells().isEmpty()) {
            throw new EmptyCourseCellsSetException();
        }

        if (planPolslResponse.timeCells().isEmpty()) {
            throw new EmptyTimeCellSetException();
        }

        producer.produceRawCoursesMessage(
                message.scheduleId(),
                planPolslResponse.courseCells(),
                planPolslResponse.timeCells()
        );
    }
}
