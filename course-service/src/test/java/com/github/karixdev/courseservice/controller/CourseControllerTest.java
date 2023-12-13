package com.github.karixdev.courseservice.controller;

import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CourseService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testInvalidDataReturns400() throws Exception {
        CourseRequest dto = CourseRequest.builder()
                .startsAt(null)
                .endsAt(null)
                .name(null)
                .courseType(null)
                .teachers("John Doe")
                .dayOfWeek(null)
                .weekType(null)
                .classrooms("Room 101")
                .additionalInfo("Additional information")
                .scheduleId(null)
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.startsAt").isNotEmpty(),
                        jsonPath("$.constraints.endsAt").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.courseType").isNotEmpty(),
                        jsonPath("$.constraints.dayOfWeek").isNotEmpty(),
                        jsonPath("$.constraints.weekType").isNotEmpty(),
                        jsonPath("$.constraints.scheduleId").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenInvalidCourseRequest_WhenUpdate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        CourseRequest dto = CourseRequest.builder()
                .startsAt(null)
                .endsAt(null)
                .name(null)
                .courseType(null)
                .teachers("John Doe")
                .dayOfWeek(null)
                .weekType(null)
                .classrooms("Room 101")
                .additionalInfo("Additional information")
                .scheduleId(null)
                .build();

        UUID id = UUID.randomUUID();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/courses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.startsAt").isNotEmpty(),
                        jsonPath("$.constraints.endsAt").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.courseType").isNotEmpty(),
                        jsonPath("$.constraints.dayOfWeek").isNotEmpty(),
                        jsonPath("$.constraints.weekType").isNotEmpty(),
                        jsonPath("$.constraints.scheduleId").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }
}