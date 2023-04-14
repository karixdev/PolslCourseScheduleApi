package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.course.Course;
import com.github.karixdev.scheduleservice.course.CourseRepository;
import com.github.karixdev.scheduleservice.course.CourseType;
import com.github.karixdev.scheduleservice.course.WeekType;
import com.github.karixdev.scheduleservice.schedule.message.ScheduleUpdateRequestMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_REQUEST_QUEUE;
import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_RESPONSE_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    CourseRepository courseRepository;

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();

        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_RESPONSE_QUEUE, true);
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_REQUEST_QUEUE, true);
    }

    @Test
    void shouldNotCreateScheduleForStandardUser() {
        String token = getUserToken();

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(scheduleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateSchedule() {
        String token = getAdminToken();

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule-name")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(scheduleRepository.findAll())
                .hasSize(1);
    }

    @Test
    void shouldCreateSchedule() {
        String token = getAdminToken();

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "available-name",
                    "group_number": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.semester").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("available-name")
                .jsonPath("$.group_number").isEqualTo(1);

        List<Schedule> schedules = scheduleRepository.findAll();

        assertThat(schedules)
                .hasSize(1);

        Schedule schedule = schedules.get(0);

        assertThat(schedule.getType())
                .isEqualTo(1);
        assertThat(schedule.getPlanPolslId())
                .isEqualTo(1999);
        assertThat(schedule.getSemester())
                .isEqualTo(2);
        assertThat(schedule.getName())
                .isEqualTo("available-name");
        assertThat(schedule.getGroupNumber())
                .isEqualTo(1);

        var expectedMsg = new ScheduleUpdateRequestMessage(
                schedule.getId(),
                schedule.getType(),
                schedule.getPlanPolslId(),
                schedule.getWd()
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(getScheduleUpdateRequestMessage())
                        .isEqualTo(expectedMsg)
                );
    }

    @Test
    void shouldGetAllCourses() {
        scheduleRepository.saveAll(List.of(
                Schedule.builder()
                        .type(1)
                        .planPolslId(2000)
                        .semester(1)
                        .name("schedule1")
                        .groupNumber(2)
                        .wd(1)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1999)
                        .semester(1)
                        .name("schedule2")
                        .groupNumber(1)
                        .wd(4)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1000)
                        .semester(2)
                        .name("schedule3")
                        .groupNumber(1)
                        .wd(0)
                        .build()
        ));

        webClient.get().uri("/api/schedules")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.*").isArray()
                .jsonPath("$[0].semester").isEqualTo(1)
                .jsonPath("$[0].group_number").isEqualTo(1)
                .jsonPath("$[1].semester").isEqualTo(1)
                .jsonPath("$[1].group_number").isEqualTo(2)
                .jsonPath("$[2].semester").isEqualTo(2)
                .jsonPath("$[2].group_number").isEqualTo(1);
    }

    @Test
    void shouldNotFindScheduleById() {
        String id = UUID.randomUUID().toString();

        webClient.get().uri("/api/schedules/" + id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo(
                        String.format(
                                "Schedule with id %s not found",
                                id
                        ));
    }

    @Test
    void shouldFindScheduleById() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(2)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        webClient.get().uri("/api/schedules/" + schedule.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("schedule1")
                .jsonPath("$.group_number").isEqualTo(2);
    }

    @Test
    void shouldNotUpdateScheduleForStandardUser() {
        String token = getUserToken();
        UUID id = UUID.randomUUID();

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotUpdateNotExistingSchedule() {
        String token = getAdminToken();
        UUID id = UUID.randomUUID();

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotUpdateScheduleWhileTryingToAssignUnavailableName() {
        String token = getAdminToken();

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(4)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule2",
                    "group_number": 1
                }
                """;

        webClient.put().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldUpdateSchedule() {
        String token = getAdminToken();

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(0)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 2,
                    "plan_polsl_id": 1999,
                    "semester": 5,
                    "name": "schedule3",
                    "group_number": 7,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(5)
                .jsonPath("$.name").isEqualTo("schedule3")
                .jsonPath("$.group_number").isEqualTo(7);

        Optional<Schedule> optionalSchedule =
                scheduleRepository.findById(schedule.getId());

        assertThat(optionalSchedule).isNotEmpty();

        schedule = optionalSchedule.get();

        var expectedMsg = new ScheduleUpdateRequestMessage(
                schedule.getId(),
                schedule.getType(),
                schedule.getPlanPolslId(),
                schedule.getWd()
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(getScheduleUpdateRequestMessage())
                        .isEqualTo(expectedMsg)
                );
    }

    @Test
    void shouldNotDeleteScheduleForStandardUser() {
        String token = getUserToken();

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotDeleteNotExistingSchedule() {
        String token = getAdminToken();

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteSchedule() {
        String token = getAdminToken();

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldNotRequestScheduleCoursesUpdateForNotExistingSchedule() {
        String token = getAdminToken();

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.post().uri("/api/schedules/" + UUID.randomUUID() + "/courses/update")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldRequestScheduleCourses() {
        String token = getAdminToken();

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.post().uri("/api/schedules/" + schedule.getId() + "/courses/update")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        var expectedMsg = new ScheduleUpdateRequestMessage(
                schedule.getId(),
                schedule.getType(),
                schedule.getPlanPolslId(),
                schedule.getWd()
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(getScheduleUpdateRequestMessage())
                        .isEqualTo(expectedMsg)
                );
    }

    private ScheduleUpdateRequestMessage getScheduleUpdateRequestMessage() {
        var typeReference = new ParameterizedTypeReference<ScheduleUpdateRequestMessage>() {
            @Override
            public @NotNull Type getType() {
                return super.getType();
            }
        };

        return rabbitTemplate.receiveAndConvert(SCHEDULE_UPDATE_REQUEST_QUEUE, typeReference);
    }

    @Test
    void shouldRetrieveAllScheduleCoursesInCorrectOrder() {
        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1337)
                .semester(1)
                .groupNumber(2)
                .name("schedule-1")
                .wd(4)
                .build();

        scheduleRepository.save(schedule);

        Course course1 = Course.builder()
                .schedule(schedule)
                .name("course-name-1")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .schedule(schedule)
                .name("course-name-2")
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course3 = Course.builder()
                .schedule(schedule)
                .name("course-name-3")
                .courseType(CourseType.LECTURE)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.saveAll(List.of(course1, course2, course3));

        webClient.get().uri("/api/schedules/" + schedule.getId() + "/courses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(course3.getId().toString())
                .jsonPath("$[1].id").isEqualTo(course2.getId().toString())
                .jsonPath("$[2].id").isEqualTo(course1.getId().toString());
    }
}
