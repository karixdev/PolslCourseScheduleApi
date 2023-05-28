package com.github.karixdev.webscraperservice.schedule;

import com.github.karixdev.webscraperservice.planpolsl.PlanPolslService;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateRequestMessage;
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
