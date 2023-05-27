package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.message.MappedCoursesMessage;
import com.github.karixdev.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.karixdev.courseservice.props.MappedCoursesMQProperties.DOMAIN_COURSE_QUEUE;

@Component
@RequiredArgsConstructor
public class CourseMessageConsumer {
    private final CourseMapper courseMapper;
    private final CourseService courseService;

    @RabbitListener(queues = DOMAIN_COURSE_QUEUE)
    private void listenForMappedCoursesMessage(MappedCoursesMessage message) {
        Set<Course> retrievedCourses = message.courses().stream()
                .map(course -> courseMapper.map(course, message.scheduleId()))
                .collect(Collectors.toSet());

        courseService.handleMappedCourses(message.scheduleId(), retrievedCourses);
    }
}
