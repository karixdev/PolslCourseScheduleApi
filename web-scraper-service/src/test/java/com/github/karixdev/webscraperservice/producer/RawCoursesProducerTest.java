package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.webscraperservice.message.RawCoursesMessage;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.TimeCell;
import com.github.karixdev.webscraperservice.props.CoursesMQProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.COURSES_EXCHANGE;
import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.RAW_COURSES_ROUTING_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RawCoursesProducerTest {
    @InjectMocks
    RawCoursesProducer underTest;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void GivenScheduleIdCourseCellsTimeCells_WhenProducerRawCoursesMessage_ThenProducesCorrectMessageToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<TimeCell> timeCells = Set.of(
                new TimeCell("08:30-10:00")
        );
        Set<CourseCell> courseCells = Set.of(
                new CourseCell(
                        10,
                        10,
                        10,
                        10,
                        "text"
                )
        );

        // When
        underTest.produceRawCoursesMessage(
                scheduleId,
                courseCells,
                timeCells
        );

        // Then
        RawCoursesMessage expectedMessage = new RawCoursesMessage(
                scheduleId,
                timeCells,
                courseCells
        );

        verify(rabbitTemplate).convertAndSend(
                eq(COURSES_EXCHANGE),
                eq(RAW_COURSES_ROUTING_KEY),
                eq(expectedMessage)
        );
    }
}