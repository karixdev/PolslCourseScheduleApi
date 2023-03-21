package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.course.Course;
import com.github.karixdev.scheduleservice.course.CourseMapper;
import com.github.karixdev.scheduleservice.course.CourseService;
import com.github.karixdev.scheduleservice.schedule.message.ScheduleUpdateResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
public class ScheduleConsumer {
    private final CourseMapper mapper;
    private final ScheduleService scheduleService;
    private final CourseService courseService;

    @RabbitListener(queues = SCHEDULE_UPDATE_RESPONSE_QUEUE)
    private void listenForScheduleCoursesUpdateResponse(ScheduleUpdateResponseMessage message) {
        Schedule schedule = scheduleService.findByIdOrElseThrow(message.id(), true);

        Set<Course> retrievedCourses = message.courses().stream()
                .map(course -> mapper.map(course, schedule))
                .collect(Collectors.toSet());

        courseService.updateScheduleCourses(schedule, retrievedCourses);
    }
}
