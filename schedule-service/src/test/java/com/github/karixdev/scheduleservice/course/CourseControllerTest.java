package com.github.karixdev.scheduleservice.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karixdev.scheduleservice.course.dto.BaseCourseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CourseControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CourseService service;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testInvalidDataReturns400() throws Exception {
        BaseCourseDTO dto = BaseCourseDTO.builder()
                .startsAt(null)
                .endsAt(null)
                .name(null)
                .courseType(null)
                .teachers("John Doe")
                .dayOfWeek(null)
                .weekType(null)
                .classrooms("Room 101")
                .additionalInfo("Additional information")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.starts_at").isNotEmpty(),
                        jsonPath("$.constraints.ends_at").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.course_type").isNotEmpty(),
                        jsonPath("$.constraints.day_of_week").isNotEmpty(),
                        jsonPath("$.constraints.week_type").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenInvalidCourseRequest_WhenUpdate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        BaseCourseDTO dto = BaseCourseDTO.builder()
                .startsAt(null)
                .endsAt(null)
                .name(null)
                .courseType(null)
                .teachers("John Doe")
                .dayOfWeek(null)
                .weekType(null)
                .classrooms("Room 101")
                .additionalInfo("Additional information")
                .build();

        UUID id = UUID.randomUUID();

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/courses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.starts_at").isNotEmpty(),
                        jsonPath("$.constraints.ends_at").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.course_type").isNotEmpty(),
                        jsonPath("$.constraints.day_of_week").isNotEmpty(),
                        jsonPath("$.constraints.week_type").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }
}
