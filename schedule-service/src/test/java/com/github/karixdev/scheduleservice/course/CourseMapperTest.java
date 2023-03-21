package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.message.CourseMessage;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseMapperTest {
    CourseMapper underTest = new CourseMapper();

    @Test
    void GivenCourseMessageAndSchedule_WhenMap_ThenReturnsCorrectCourseMapper() {
        // Given
        var courseMessage = new CourseMessage(
                LocalTime.of(8, 30),
                LocalTime.of(10, 15),
                "Calculus",
                CourseType.LAB,
                Set.of("dr Adam", "mgr Marcin"),
                DayOfWeek.FRIDAY,
                WeekType.EVEN,
                Set.of("314 RMS", "CEK Room C"),
                null
        );

        var schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule")
                .wd(0)
                .build();

        // When
        var result = underTest.map(courseMessage, schedule);

        // Then
        assertThat(result.getName())
                .isEqualTo("Calculus");

        assertThat(result.getCourseType())
                .isEqualTo(CourseType.LAB);

        assertThat(result.getAdditionalInfo())
                .isNull();

        assertThat(result.getDayOfWeek())
                .isEqualTo(DayOfWeek.FRIDAY);

        assertThat(result.getStartsAt())
                .isEqualTo(LocalTime.of(8, 30));

        assertThat(result.getEndsAt())
                .isEqualTo(LocalTime.of(10, 15));

        assertThat(result.getTeachers())
                .isIn("dr Adam, mgr Marcin", "mgr Marcin, dr Adam");

        assertThat(result.getClassroom())
                .isIn("314 RMS, CEK Room C", "CEK Room C, 314 RMS");

        assertThat(result.getSchedule())
                .isEqualTo(schedule);

        assertThat(result.getWeekType())
                .isEqualTo(WeekType.EVEN);
    }
}
