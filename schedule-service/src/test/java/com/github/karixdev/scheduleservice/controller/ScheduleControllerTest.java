package com.github.karixdev.scheduleservice.controller;

import com.github.karixdev.scheduleservice.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ScheduleController.class)
public class ScheduleControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ScheduleService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void GivenInvalidScheduleRequest_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                -1,
                0,
                0,
                "",
                0
        );

        String content = mapper.writeValueAsString(scheduleRequest);

        // When & Then
        mockMvc.perform(post("/api/v2/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.type").isNotEmpty(),
                        jsonPath("$.constraints.plan_polsl_id").isNotEmpty(),
                        jsonPath("$.constraints.semester").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.group_number").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenUnavailableName_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1,
                1,
                "unavailable",
                1
        );

        String content = mapper.writeValueAsString(scheduleRequest);

        RuntimeException exception = new ScheduleNameUnavailableException("unavailable");

        when(service.create(eq(scheduleRequest)))
                .thenThrow(exception);

        // When & Then
        mockMvc.perform(post("/api/v2/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.name").value(exception.getMessage()),
                        jsonPath("$.message").value("Validation Failed")
                );
    }
}
