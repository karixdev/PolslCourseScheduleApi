package com.github.karixdev.domaincoursemapperservice.consumer;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.domaincoursemapperservice.service.CourseService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleRawConsumerTest {

    @InjectMocks
    ScheduleRawConsumer underTest;

    @Mock
    CourseService courseService;

    @Test
    void GivenConsumerRecord_WhenConsumeScheduleRaw_ThenRecordIsHandledByService() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        CourseCell courseCell = CourseCell.builder()
                .top(259)
                .left(254)
                .ch(135)
                .cw(154)
                .text("course 1")
                .build();

        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(scheduleId)
                .courseCells(Set.of(courseCell))
                .timeCells(Set.of(new TimeCell("08:30-10:00")))
                .build();

        ConsumerRecord<String, ScheduleRaw> consumerRecord = new ConsumerRecord<>("topic", 0, 0, scheduleId, scheduleRaw);

        // When
        underTest.consumeScheduleRaw(consumerRecord);

        // Then
        verify(courseService).handleScheduleRaw(consumerRecord);
    }

}