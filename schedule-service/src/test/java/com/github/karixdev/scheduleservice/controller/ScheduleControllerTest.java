package com.github.karixdev.scheduleservice.controller;

import com.github.karixdev.commonservice.dto.schedule.ScheduleRequest;
import com.github.karixdev.scheduleservice.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(controllers = ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ScheduleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ScheduleService service;

    ObjectMapper mapper = new ObjectMapper();

    ScheduleRequest invalidScheduleRequest;
    ScheduleRequest validScheduleRequest;

    @BeforeEach
    void setUp() {
        invalidScheduleRequest = new ScheduleRequest(
                -1,
                0,
                0,
                "",
                0,
                -1
        );
        validScheduleRequest = new ScheduleRequest(
                1,
                1,
                1,
                "unavailable",
                1,
                0
        );
    }

    @Test
    void GivenInvalidScheduleRequest_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        String content = mapper.writeValueAsString(invalidScheduleRequest);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.type").isNotEmpty(),
                        jsonPath("$.constraints.planPolslId").isNotEmpty(),
                        jsonPath("$.constraints.semester").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.groupNumber").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenInvalidScheduleRequest_WhenUpdate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        String content = mapper.writeValueAsString(invalidScheduleRequest);

        // When & Then
        mockMvc.perform(put("/api/schedules/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.type").isNotEmpty(),
                        jsonPath("$.constraints.planPolslId").isNotEmpty(),
                        jsonPath("$.constraints.semester").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.groupNumber").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

}
