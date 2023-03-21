package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.schedule.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
    @InjectMocks
    CourseService underTest;

    @Mock
    CourseRepository repository;

    @Test
    void GivenScheduleAndSetOfRetrievedCourses_WhenUpdateScheduleCourses_ThenSavesAndDeletesProperCourses() {
        // Given
        var course1 = Course.builder()
                .name("Calculus I")
                .courseType(CourseType.PRACTICAL)
                .teachers("dr. Adam")
                .classroom("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        var course2 = Course.builder()
                .name("Physics")
                .courseType(CourseType.LAB)
                .teachers("dr. Max")
                .classroom("408MS")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        var course3 = Course.builder()
                .name("C++")
                .courseType(CourseType.LECTURE)
                .teachers("dr. Henryk")
                .classroom("CEK Room C")
                .additionalInfo("contact teacher")
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        var retrievedCourses = Set.of(course1, course2);

        var schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1333)
                .semester(3)
                .name("Schedule")
                .groupNumber(1)
                .wd(4)
                .courses(Set.of(course2, course3))
                .build();

        // When
        underTest.updateScheduleCourses(schedule, retrievedCourses);

        // Then
        verify(repository).deleteAll(Set.of(course3));
        verify(repository).saveAll(Set.of(course1));
    }
}
