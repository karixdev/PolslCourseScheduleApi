package com.github.karixdev.webhookservice.controller;

import com.github.karixdev.webhookservice.dto.WebhookRequest;
import com.github.karixdev.webhookservice.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebhookControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    WebhookService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void GivenInvalidRequest_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "",
                Set.of()
        );

        String content = mapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.url").isNotEmpty(),
                        jsonPath("$.constraints.schedules").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenInvalidRequest_WhenUpdate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "",
                Set.of()
        );

        String content = mapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/webhooks/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.url").isNotEmpty(),
                        jsonPath("$.constraints.schedules").isNotEmpty(),
                        jsonPath("$.message").value("Validation Failed")
                );
    }
}