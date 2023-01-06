package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karixdev.polslcoursescheduleapi.course.Course;
import com.github.karixdev.polslcoursescheduleapi.course.Weeks;
import com.github.karixdev.polslcoursescheduleapi.course.payload.response.CourseResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleController;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleCollectionResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleWithCoursesResponse;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ScheduleController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class ScheduleControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ScheduleService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void GivenInvalidPayload_WhenAdd_ThenRespondsWithBadRequestStatus() throws Exception {
        ScheduleRequest payload = new ScheduleRequest(
                -1,
                -2,
                -3,
                "",
                -4
        );

        String content = mapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenUnavailableName_WhenAdd_ThenRespondsWithConflictStatus() throws Exception {
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );

        String content = mapper.writeValueAsString(payload);

        doThrow(ScheduleNameNotAvailableException.class)
                .when(service)
                .add(any(), any());

        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict());
    }

    @Test
    void GivenValidPayload_WhenAdd_ThenRespondsWithCreatedStatusAndProperBody() throws Exception {
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );

        String content = mapper.writeValueAsString(payload);

        when(service.add(any(), any())).thenReturn(new ScheduleResponse(
                1L,
                3,
                "schedule-name",
                4
        ));


        mockMvc.perform(post("/api/v1/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.id").value(1),
                        jsonPath("$.semester").value(3),
                        jsonPath("$.name").value("schedule-name"),
                        jsonPath("$.group_number").value(4)
                );
    }

    @Test
    void GivenNotExistingScheduleId_WhenDelete_ThenRespondsWithNotFoundStatus() throws Exception {
        Long id = 1L;

        doThrow(ResourceNotFoundException.class)
                .when(service)
                .delete(eq(id));

        mockMvc.perform(delete("/api/v1/schedule/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenExistingScheduleId_WhenDelete_ThenRespondsWithOkStatusAndSuccessMessage() throws Exception {
        Long id = 1L;

        when(service.delete(eq(id)))
                .thenReturn(new SuccessResponse());

        mockMvc.perform(delete("/api/v1/schedule/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void WhenGetAll_ThenRespondsWithOkStatusAndProperBody() throws Exception {
        when(service.getAll())
                .thenReturn(new ScheduleCollectionResponse(
                        Map.of(
                                1, List.of(
                                        new ScheduleResponse(
                                                1L, 1, "schedule-1", 2)),
                                2, List.of(
                                        new ScheduleResponse(
                                                2L, 2, "schedule-2", 3))
                        )
                ));

        mockMvc.perform(get("/api/v1/schedule")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.semesters.1[0].id").value(1),
                        jsonPath("$.semesters.1[0].group_number").value(2),
                        jsonPath("$.semesters.1[0].name").value("schedule-1"),
                        jsonPath("$.semesters.2[0].id").value(2),
                        jsonPath("$.semesters.2[0].group_number").value(3),
                        jsonPath("$.semesters.2[0].name").value("schedule-2")
                );
    }

    @Test
    void GivenNotExistingScheduleId_WhenGetScheduleWithCourses_ThenRespondsWithNotFoundStatus() throws Exception {
        Long id = 1337L;

        doThrow(ResourceNotFoundException.class).when(service)
                .getSchedulesWithCourses(eq(id));

        mockMvc.perform(get("/api/v1/schedule/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenExistingScheduleId_WhenGetScheduleWithCourses_ThenRespondsWithOkStatusAndProperBody() throws Exception {
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

        when(service.getSchedulesWithCourses(eq(schedule.getId())))
                .thenReturn(payload);

        mockMvc.perform(get("/api/v1/schedule/" + schedule.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(1),
                        jsonPath("$.semester").value(2),
                        jsonPath("$.name").value("schedule-name"),
                        jsonPath("$.group_number").value(3),
                        jsonPath("$.courses.MONDAY[0].description")
                                .value("course"),
                        jsonPath("$.courses.MONDAY[0].starts_at")
                                .value("08:30:00"),
                        jsonPath("$.courses.MONDAY[0].ends_at")
                                .value("10:00:00"),
                        jsonPath("$.courses.MONDAY[0].weeks")
                                .value("EVERY"),
                        jsonPath("$.courses.FRIDAY[0].description")
                                .value("course-2"),
                        jsonPath("$.courses.FRIDAY[0].starts_at")
                                .value("10:15:00"),
                        jsonPath("$.courses.FRIDAY[0].ends_at")
                                .value("11:45:00"),
                        jsonPath("$.courses.FRIDAY[0].weeks")
                                .value("EVEN")
                );
    }

    @Test
    void GivenNotExistingScheduleId_WhenManualUpdate_ThenRespondsWithNotFoundStatus() throws Exception {
        Long id = 1337L;

        doThrow(ResourceNotFoundException.class).when(service)
                .manualUpdate(eq(id));

        mockMvc.perform(post("/api/v1/schedule/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenExistingScheduleId_WhenManualUpdate_ThenRespondsNoContentStatus() throws Exception {
        Long id = 1337L;

        doNothing()
                .when(service)
                .manualUpdate(eq(id));

        mockMvc.perform(post("/api/v1/schedule/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
