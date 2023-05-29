package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.webscraperservice.message.RawCoursesMessage;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.TimeCell;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.COURSES_EXCHANGE;
import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.RAW_COURSES_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class RawCoursesProducer {
    private final RabbitTemplate rabbitTemplate;

    public void produceRawCoursesMessage(
            UUID scheduleId,
            Set<CourseCell> courseCells,
            Set<TimeCell> timeCells
    ) {
        RawCoursesMessage message = new RawCoursesMessage(
                scheduleId,
                timeCells,
                courseCells
        );

        rabbitTemplate.convertAndSend(
                COURSES_EXCHANGE,
                RAW_COURSES_ROUTING_KEY,
                message
        );
    }
}
