package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import com.github.karixdev.scheduleservice.schedule.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CourseControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    void shouldNotCreateCourseForNotExistingSchedule() {

        scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule")
                .wd(4)
                .build());

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.post().uri("/api/v2/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Schedule with id 11111111-1111-1111-1111-111111111111 not found");

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateCourse() {
        scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-1")
                .wd(4)
                .build());

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1337)
                .semester(1)
                .groupNumber(2)
                .name("schedule-2")
                .wd(4)
                .build());

        String payload = """
                {
                    "schedule_id": "%s",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """.formatted(schedule.getId());

        webClient.post().uri("/api/v2/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course course = courses.get(0);

        assertThat(course.getSchedule()).isEqualTo(schedule);
        assertThat(course.getName()).isEqualTo("course-name");
        assertThat(course.getCourseType()).isEqualTo(CourseType.LAB);
        assertThat(course.getTeachers()).isEqualTo("dr Adam");
        assertThat(course.getClassroom()).isEqualTo("LAB 1");
        assertThat(course.getAdditionalInfo()).isEqualTo("Only on 3.08");
        assertThat(course.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(course.getWeekType()).isEqualTo(WeekType.EVEN);
        assertThat(course.getStartsAt()).isEqualTo(LocalTime.of(8, 30));
        assertThat(course.getEndsAt()).isEqualTo(LocalTime.of(10, 15));
    }
}
