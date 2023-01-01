package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.course.exception.EmptyCourseCellListException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.TimeCell;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
    @InjectMocks
    CourseService underTest;

    @Mock
    CourseRepository repository;

    @Mock
    CourseMapper mapper;

    Schedule schedule;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();
    }

    @Test
    void GivenPlanPolslResponseWithEmptyCourseCellList_WhenUpdateScheduleCourses_ThenThrowsEmptyCourseCellListExceptionWithCorrectMessage() {
        // Given
        PlanPolslResponse response = new PlanPolslResponse(
                List.of(new TimeCell("07:00-08:00")),
                List.of()
        );

        // When & Then
        assertThatThrownBy(() -> underTest.updateScheduleCourses(
                response, schedule))
                .isInstanceOf(EmptyCourseCellListException.class)
                .hasMessage("Course cell list is empty");
    }

    @Test
    void GivenPlanPolslResponseAndSchedule_WhenUpdateScheduleCourses_ThenSavesListWithCoursesAndDeletesOldCourses() {
        // Given
        PlanPolslResponse response = new PlanPolslResponse(
                List.of(new TimeCell("07:00-08:00")),
                List.of(new CourseCell(304, 420, 56, 154, "course"))
        );

        Course course = Course.builder()
                .schedule(schedule)
                .weeks(Weeks.EVERY)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 0))
                .build();

        when(mapper.mapCellToCourse(any(), anyInt(), any()))
                .thenReturn(course);

        // When
        underTest.updateScheduleCourses(response, schedule);

        // Then
        verify(repository).deleteAll(any());
        verify(repository).saveAll(eq(List.of(course)));
    }
}
