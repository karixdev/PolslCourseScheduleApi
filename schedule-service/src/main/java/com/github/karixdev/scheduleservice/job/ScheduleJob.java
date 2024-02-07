package com.github.karixdev.scheduleservice.job;

import com.github.karixdev.scheduleservice.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleJob {

    private final ScheduleService scheduleService;

    @Scheduled(cron = "${schedule.job.cron}")
    private void updateScheduleCourses() {
        scheduleService.requestScheduleCoursesUpdateForAll();
    }

}
