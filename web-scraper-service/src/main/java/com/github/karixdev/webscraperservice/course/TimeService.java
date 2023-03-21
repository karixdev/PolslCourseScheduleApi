package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.schedule.exception.NoScheduleStartTimeException;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;

@Service
public class TimeService {
    public LocalTime getScheduleStartTime(Set<TimeCell> timeCells) {
        return timeCells.stream()
                .map(timeCell -> LocalTime.parse(timeCell.text().split("-")[0]))
                .min(LocalTime::compareTo)
                .orElseThrow(() -> {
                    throw new NoScheduleStartTimeException();
                });
    }
}
