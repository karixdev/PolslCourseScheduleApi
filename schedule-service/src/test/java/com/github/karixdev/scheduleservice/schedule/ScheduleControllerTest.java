package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        String content = mapper.writeValueAsString(validScheduleRequest);
        RuntimeException exception = new ScheduleNameUnavailableException("unavailable");

        when(service.create(eq(validScheduleRequest)))
                .thenThrow(exception);

        // When & Then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.name").value(exception.getMessage()),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenNotExistingScheduleId_WhenFindById_ThenRespondsWithNotFoundAndProperBody() throws Exception {
        // Given
        UUID id = UUID.randomUUID();

        RuntimeException exception = new ResourceNotFoundException("Exception message");

        when(service.findById(eq(id)))
                .thenThrow(exception);

        // When & Then
        mockMvc.perform(get("/api/schedules/" + id))
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.status").value(404),
                        jsonPath("$.message").value("Exception message")
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
                        jsonPath("$.constraints.plan_polsl_id").isNotEmpty(),
                        jsonPath("$.constraints.semester").isNotEmpty(),
                        jsonPath("$.constraints.name").isNotEmpty(),
                        jsonPath("$.constraints.group_number").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenUnavailableName_WhenUpdate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        String content = mapper.writeValueAsString(validScheduleRequest);

        RuntimeException exception = new ScheduleNameUnavailableException("unavailable");

        when(service.update(eq(id), eq(validScheduleRequest)))
                .thenThrow(exception);

        // When & Then
        mockMvc.perform(put("/api/schedules/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.name").value(exception.getMessage()),
                        jsonPath("$.message").value("Validation Failed")
                );
    }
}
