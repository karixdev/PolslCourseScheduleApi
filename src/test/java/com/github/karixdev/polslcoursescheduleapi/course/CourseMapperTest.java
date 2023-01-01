package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseMapperTest {
    CourseMapper underTest =
            new CourseMapper(new CourseProperties());

    @Test
    void GivenCourseCellAndStartTimeHourAndSchedule_WhenMapCellToCourse_ThenReturnsCorrectCourse() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        Schedule schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();

        int startTimeHour = 7;

        CourseCell courseCell = new CourseCell(
                304, 420, 56, 154, "course"
        );

        // When
        Course course = underTest.mapCellToCourse(
                courseCell, startTimeHour, schedule);

        // Then
        assertThat(course.getStartsAt()).isEqualTo(LocalTime.of(8, 30));
        assertThat(course.getEndsAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(course.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(course.getWeeks()).isEqualTo(Weeks.EVERY);
        assertThat(course.getSchedule()).isEqualTo(schedule);
        assertThat(course.getDescription()).isEqualTo("course");
    }
}
