package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import com.github.karixdev.scheduleservice.schedule.ScheduleRepository;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

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

    @Test
    void shouldNotDeleteNotExistingCourse() {
        webClient.delete().uri("/api/v2/courses/11111111-1111-1111-1111-111111111111")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Course with id 11111111-1111-1111-1111-111111111111 not found");
    }

    @Test
    void shouldDeleteCourse() {
        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1337)
                .semester(1)
                .groupNumber(2)
                .name("schedule-2")
                .wd(4)
                .build();

        scheduleRepository.save(schedule);

        Course course = Course.builder()
                .schedule(schedule)
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.save(course);

        webClient.delete().uri("/api/v2/courses/" + course.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotUpdateNotExistingCourse() {
        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111112",
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

        webClient.put().uri("/api/v2/courses/11111111-1111-1111-1111-111111111111")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Course with id 11111111-1111-1111-1111-111111111111 not found");
    }

    @Test
    void shouldNotUpdateCourseWithNotExistingSchedule() {
        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1337)
                .semester(1)
                .groupNumber(2)
                .name("schedule-2")
                .wd(4)
                .build();

        scheduleRepository.save(schedule);

        Course course = Course.builder()
                .schedule(schedule)
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.save(course);

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111112",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "updated-course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.put().uri("/api/v2/courses/" + course.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Schedule with id 11111111-1111-1111-1111-111111111112 not found");
    }

    @Test
    void shouldUpdateCourse() {
        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1337)
                .semester(1)
                .groupNumber(2)
                .name("schedule-1")
                .wd(4)
                .build();

        Schedule schedule2 = Schedule.builder()
                .type(0)
                .planPolslId(1447)
                .semester(2)
                .groupNumber(4)
                .name("schedule-2")
                .wd(4)
                .build();

        scheduleRepository.saveAll(List.of(schedule, schedule2));

        Course course = Course.builder()
                .schedule(schedule)
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.save(course);

        String payload = """
                {
                    "schedule_id": "%s",
                    "starts_at": "10:30",
                    "ends_at": "12:00",
                    "name": "updated-course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """.formatted(schedule2.getId());

        webClient.put().uri("/api/v2/courses/" + course.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();

        List<Course> resultCourseList = courseRepository.findAll();
        assertThat(resultCourseList).hasSize(1);

        Course resultCourse = resultCourseList.get(0);

        assertThat(resultCourse.getSchedule()).isEqualTo(schedule2);
        assertThat(resultCourse.getName()).isEqualTo("updated-course-name");
        assertThat(resultCourse.getCourseType()).isEqualTo(CourseType.LAB);
        assertThat(resultCourse.getTeachers()).isEqualTo("dr Adam");
        assertThat(resultCourse.getClassroom()).isEqualTo("LAB 1");
        assertThat(resultCourse.getAdditionalInfo()).isEqualTo("Only on 3.08");
        assertThat(resultCourse.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(resultCourse.getWeekType()).isEqualTo(WeekType.EVEN);
        assertThat(resultCourse.getStartsAt()).isEqualTo(LocalTime.of(10, 30));
        assertThat(resultCourse.getEndsAt()).isEqualTo(LocalTime.of(12, 0));
    }
}
