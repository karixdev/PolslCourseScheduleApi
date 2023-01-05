package com.github.karixdev.polslcoursescheduleapi.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleJob {
    private final ScheduleService scheduleService;

    @Scheduled(cron = "${schedule-job.cron}")
//    @Scheduled(fixedRate = 60000)
    private void scheduledUpdate() {
        scheduleService.updateSchedules();
    }
}
