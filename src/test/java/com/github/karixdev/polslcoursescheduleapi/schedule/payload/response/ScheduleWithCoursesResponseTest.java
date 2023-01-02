package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import com.github.karixdev.polslcoursescheduleapi.course.Course;
import com.github.karixdev.polslcoursescheduleapi.course.Weeks;
import com.github.karixdev.polslcoursescheduleapi.course.payload.response.CourseResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ScheduleWithCoursesResponseTest {
    @Autowired
    JacksonTester<ScheduleWithCoursesResponse> jTester;

    @Test
    void testSerialization() throws IOException {
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

        Map<DayOfWeek, List<CourseResponse>> map = Map.of(
                DayOfWeek.MONDAY, List.of(new CourseResponse(Course.builder()
                        .id(1L)
                        .schedule(schedule)
                        .weeks(Weeks.EVERY)
                        .description("course")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .startsAt(LocalTime.of(8, 30))
                        .endsAt(LocalTime.of(10, 0))
                        .build())),
                DayOfWeek.FRIDAY, List.of(new CourseResponse(Course.builder()
                        .id(2L)
                        .schedule(schedule)
                        .weeks(Weeks.EVEN)
                        .description("course-2")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .startsAt(LocalTime.of(10, 15))
                        .endsAt(LocalTime.of(11, 45))
                        .build()))
        );

        ScheduleWithCoursesResponse payload =
                new ScheduleWithCoursesResponse(schedule, map);

        var result = jTester.write(payload);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathValue("$.id")
                .isEqualTo(1);

        assertThat(result).hasJsonPath("$.semester");
        assertThat(result).extractingJsonPathValue("$.semester")
                .isEqualTo(2);

        assertThat(result).hasJsonPath("$.name");
        assertThat(result).extractingJsonPathValue("$.name")
                .isEqualTo("schedule-name");

        assertThat(result).hasJsonPath("$.group_number");
        assertThat(result).extractingJsonPathValue("$.group_number")
                .isEqualTo(3);

        assertThat(result).hasJsonPathArrayValue("$.courses.MONDAY");
        assertThat(result).extractingJsonPathArrayValue("$.courses.MONDAY")
                .hasSize(1);

        assertThat(result).hasJsonPath("$.courses.MONDAY[0].description");
        assertThat(result).extractingJsonPathValue("$.courses.MONDAY[0].description")
                .isEqualTo("course");

        assertThat(result).hasJsonPath("$.courses.MONDAY[0].starts_at");
        assertThat(result).extractingJsonPathValue("$.courses.MONDAY[0].starts_at")
                .isEqualTo("08:30:00");

        assertThat(result).hasJsonPath("$.courses.MONDAY[0].ends_at");
        assertThat(result).extractingJsonPathValue("$.courses.MONDAY[0].ends_at")
                .isEqualTo("10:00:00");

        assertThat(result).hasJsonPath("$.courses.MONDAY[0].weeks");
        assertThat(result).extractingJsonPathValue("$.courses.MONDAY[0].weeks")
                .isEqualTo("EVERY");

        assertThat(result).hasJsonPathArrayValue("$.courses.FRIDAY");
        assertThat(result).extractingJsonPathArrayValue("$.courses.FRIDAY")
                .hasSize(1);

        assertThat(result).hasJsonPath("$.courses.FRIDAY[0].description");
        assertThat(result).extractingJsonPathValue("$.courses.FRIDAY[0].description")
                .isEqualTo("course-2");

        assertThat(result).hasJsonPath("$.courses.FRIDAY[0].starts_at");
        assertThat(result).extractingJsonPathValue("$.courses.FRIDAY[0].starts_at")
                .isEqualTo("10:15:00");

        assertThat(result).hasJsonPath("$.courses.FRIDAY[0].ends_at");
        assertThat(result).extractingJsonPathValue("$.courses.FRIDAY[0].ends_at")
                .isEqualTo("11:45:00");

        assertThat(result).hasJsonPath("$.courses.FRIDAY[0].weeks");
        assertThat(result).extractingJsonPathValue("$.courses.FRIDAY[0].weeks")
                .isEqualTo("EVEN");
    }
}
