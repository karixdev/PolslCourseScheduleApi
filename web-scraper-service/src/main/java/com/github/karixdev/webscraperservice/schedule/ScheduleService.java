package com.github.karixdev.webscraperservice.schedule;

import com.github.karixdev.webscraperservice.course.CourseMapper;
import com.github.karixdev.webscraperservice.course.TimeService;
import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.planpolsl.PlanPolslClient;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final PlanPolslClient planPolslClient;
    private final TimeService timeService;
    private final CourseMapper courseMapper;
    private final ScheduleProducer scheduleProducer;

    public void updateSchedule(ScheduleUpdateRequestMessage message) {
        PlanPolslResponse planPolslResponse = planPolslClient.getSchedule(
                message.planPolslId(), message.type(), message.wd());

        if (planPolslResponse.courseCells().isEmpty()) {
            throw new EmptyCourseCellsSetException();
        }

        LocalTime scheduleStartTime =
                timeService.getScheduleStartTime(
                        planPolslResponse.timeCells());

        Set<Course> courses = planPolslResponse.courseCells().stream()
                .map(courseCell -> courseMapper.map(courseCell, scheduleStartTime))
                .collect(Collectors.toSet());

        scheduleProducer.sendScheduleUpdateResponseMessage(message.id(), courses);
    }
}
