package com.github.karixdev.courseservice.application.event.handler;

import com.github.karixdev.courseservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.updater.ScheduleCoursesUpdater;
import com.github.karixdev.courseservice.application.validator.Validator;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseWeekType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawSchedule;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedRawScheduleEventHandlerTest {

    @InjectMocks
    private ProcessedRawScheduleEventHandler underTest;

    @Mock
    ModelMapper<ProcessedRawCourse, Course> toEntityMapper;

    @Mock
    CourseRepository repository;

    @Mock
    ScheduleCoursesUpdater updater;

    @Mock
    Validator<ProcessedRawCourse> processedRawCourseValidator;

    @Test
    void GivenProcessedRawScheduleEvent_WhenHandle_ThenUpdateScheduleCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        ProcessedRawCourse processedRawCourse1 = ProcessedRawCourse.builder()
                .courseType(ProcessedRawCourseType.INFO)
                .weekType(ProcessedRawCourseWeekType.EVERY)
                .name("Calculus I")
                .scheduleId(scheduleId)
                .teachers("dr. Adam")
                .classrooms("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        ProcessedRawCourse processedRawCourse2 = ProcessedRawCourse.builder()
                .courseType(ProcessedRawCourseType.LAB)
                .weekType(ProcessedRawCourseWeekType.EVEN)
                .name("Physics")
                .scheduleId(scheduleId)
                .teachers("dr. Smith")
                .classrooms("410")
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(9, 21))
                .endsAt(LocalTime.of(12, 32))
                .build();

        ProcessedRawSchedule processedRawSchedule = ProcessedRawSchedule.builder()
                .courses(Set.of(processedRawCourse1, processedRawCourse2))
                .build();

        ProcessedRawScheduleEvent event = ProcessedRawScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .entity(processedRawSchedule)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Calculus I")
                .scheduleId(scheduleId)
                .courseType(CourseType.PRACTICAL)
                .teachers("dr. Adam")
                .classrooms("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .scheduleId(scheduleId)
                .courseType(CourseType.LAB)
                .teachers("dr. Max")
                .classrooms("408MS")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        when(processedRawCourseValidator.isValid(processedRawCourse2))
                .thenReturn(false);

        when(processedRawCourseValidator.isValid(processedRawCourse1))
                .thenReturn(true);

        when(toEntityMapper.map(processedRawCourse1))
                .thenReturn(course1);

        when(repository.findByScheduleId(scheduleId))
                .thenReturn(List.of(course2));

        // When
        underTest.handle(event);

        // Then
        verify(updater).update(Set.of(course2), Set.of(course1));
    }

}