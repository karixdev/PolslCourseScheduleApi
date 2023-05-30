package com.example.discordnotificationservice.controller;

import com.example.discordnotificationservice.dto.WebhookRequest;
import com.example.discordnotificationservice.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.exception.UnavailableDiscordApiIdException;
import com.example.discordnotificationservice.exception.UnavailableTokenException;
import com.example.discordnotificationservice.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
    void GivenRequestWithInvalidDiscordWebhookUrl_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "https://ivalid-url.com",
                Set.of(UUID.randomUUID())
        );

        String content = mapper.writeValueAsString(request);

        when(service.create(eq(request), any()))
                .thenThrow(new InvalidDiscordWebhookUrlException());

        // When & Then
        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.url").value("Provided Discord webhook url is invalid"),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenRequestWithUrlContainingUnavailableDiscordApiId_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/discordApiId/token",
                Set.of(UUID.randomUUID())
        );

        String content = mapper.writeValueAsString(request);

        when(service.create(eq(request), any()))
                .thenThrow(new UnavailableDiscordApiIdException());

        // When & Then
        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.url").value("Id in provided url is unavailable"),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenRequestWithUrlContainingUnavailableToken_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/discordApiId/token",
                Set.of(UUID.randomUUID())
        );

        String content = mapper.writeValueAsString(request);

        when(service.create(eq(request), any()))
                .thenThrow(new UnavailableTokenException());

        // When & Then
        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.url").value("Token in provided url is unavailable"),
                        jsonPath("$.message").value("Validation Failed")
                );
    }

    @Test
    void GivenRequestWithSchedulesContainingNotExistingOnes_WhenCreate_ThenRespondsWithBadRequestAndProperBody() throws Exception {
        // Given
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/discordApiId/token",
                Set.of(UUID.randomUUID())
        );

        String content = mapper.writeValueAsString(request);

        when(service.create(eq(request), any()))
                .thenThrow(new NotExistingSchedulesException());

        // When & Then
        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpectAll(
                        jsonPath("$.constraints.schedules").value("Provided set of schedules includes non-existing schedules"),
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