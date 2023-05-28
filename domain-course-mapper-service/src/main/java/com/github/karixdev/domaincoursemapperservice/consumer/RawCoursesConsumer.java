package com.github.karixdev.domaincoursemapperservice.consumer;

import com.github.karixdev.domaincoursemapperservice.message.RawCoursesMessage;
import com.github.karixdev.domaincoursemapperservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.RAW_COURSES_QUEUE;

@Component
@RequiredArgsConstructor
public class RawCoursesConsumer {
    private final CourseService courseService;

    @RabbitListener(queues = RAW_COURSES_QUEUE)
    private void listenForRawCoursesMessage(RawCoursesMessage message) {
        courseService.handleRawCoursesMessage(
                message.scheduleId(),
                message.timeCells(),
                message.courseCells()
        );
    }
}
