package com.github.karixdev.courseservice.application.updater;

import com.github.karixdev.courseservice.application.dal.TransactionCallback;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleCoursesUpdaterTest {

    @InjectMocks
    ScheduleCoursesUpdater underTest;

    @Mock
    CourseRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Test
    void GivenCurrentReceivedCoursesSets_WhenUpdate_ThenSavesAndDeletesProperCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();

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

        Course course3 = Course.builder()
                .id(UUID.randomUUID())
                .name("C++")
                .scheduleId(scheduleId)
                .courseType(CourseType.LECTURE)
                .teachers("dr. Henryk")
                .classrooms("CEK Room C")
                .additionalInfo("contact teacher")
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        Set<Course> received = Set.of(course1, course2);
        Set<Course> current = Set.of(course2, course3);

        // When
        underTest.update(current, received);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback callback = transactionCallbackCaptor.getValue();
        callback.execute();

        verify(repository).deleteAll(Set.of(course3));
        verify(repository).saveAll(Set.of(course1));
    }

}