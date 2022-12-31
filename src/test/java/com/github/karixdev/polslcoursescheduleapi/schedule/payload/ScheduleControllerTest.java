package com.github.karixdev.polslcoursescheduleapi.schedule.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleController;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
