package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.service.PlanPolslService;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.message.ScheduleUpdateRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final PlanPolslService planPolslService;

    public void updateSchedule(ScheduleUpdateRequestMessage message) {
        PlanPolslResponse planPolslResponse = planPolslService.getSchedule(
                message.planPolslId(), message.type(), message.wd());

        if (planPolslResponse.courseCells().isEmpty()) {
            throw new EmptyCourseCellsSetException();
        }
    }
}
