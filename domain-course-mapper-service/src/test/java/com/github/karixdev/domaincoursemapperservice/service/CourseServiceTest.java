package com.github.karixdev.domaincoursemapperservice.service;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseType;
import com.github.karixdev.commonservice.model.course.domain.WeekType;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.domaincoursemapperservice.exception.NoScheduleStartTimeException;
import com.github.karixdev.domaincoursemapperservice.mapper.CourseCellMapper;
import com.github.karixdev.domaincoursemapperservice.mapper.TimeCellMapper;
import com.github.karixdev.domaincoursemapperservice.producer.ScheduleDomainProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @InjectMocks
    CourseService underTest;

    @Mock
    CourseCellMapper courseCellMapper;

    @Mock
    TimeCellMapper timeCellMapper;

    @Mock
    ScheduleDomainProducer producer;

    @Test
    void GivenEmptyTimeCellSet_WhenHandleRawCoursesMessage_ThenThrowsNoScheduleStartTimeException() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(scheduleId)
                .courseCells(Set.of())
                .timeCells(Set.of())
                .build();

        ConsumerRecord<String, ScheduleRaw> consumerRecord = new ConsumerRecord<>("topic", 0, 0, scheduleId, scheduleRaw);

        // When & Then
        assertThatThrownBy(() -> underTest.handleScheduleRaw(consumerRecord))
                .isInstanceOf(NoScheduleStartTimeException.class);
    }

    @Test
    void GivenScheduleIdTimeCellSetCourseCellSet_WhenHandleRawCoursesMessage_ThenMapsCourseCellsAndProducesDomainCourseMessage() {
        // Given
        CourseCell courseCell = CourseCell.builder()
                .top(259)
                .left(254)
                .ch(135)
                .cw(154)
                .text("course 1")
                .build();
        Set<CourseCell> courseCells = Set.of(courseCell);

        TimeCell timeCell1 = new TimeCell("11:00-12:00");
        TimeCell timeCell2 = new TimeCell("08:30-10:00");
        Set<TimeCell> timeCells = Set.of(timeCell1, timeCell2);

        String scheduleId = UUID.randomUUID().toString();
        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(scheduleId)
                .courseCells(courseCells)
                .timeCells(timeCells)
                .build();

        ConsumerRecord<String, ScheduleRaw> consumerRecord = new ConsumerRecord<>("topic", 0, 0, scheduleId, scheduleRaw);

        when(timeCellMapper.mapToLocalTime(timeCell1))
                .thenReturn(LocalTime.of(11, 0));

        when(timeCellMapper.mapToLocalTime(timeCell2))
                .thenReturn(LocalTime.of(8, 30));

        CourseDomain course = CourseDomain.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(11, 45))
                .name("course 1")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weeks(WeekType.EVERY)
                .build();

        when(courseCellMapper.mapToCourse(courseCell, LocalTime.of(8, 30)))
                .thenReturn(course);

        // When
        underTest.handleScheduleRaw(consumerRecord);

        // Then
        verify(producer).produceScheduleDomain(scheduleId, Set.of(course));
    }

}