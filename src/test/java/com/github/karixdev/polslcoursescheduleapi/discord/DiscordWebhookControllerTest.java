package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookUrlNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebhookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.UpdateDiscordWebhookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebhookResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.PermissionDeniedException;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DiscordWebhookController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class DiscordWebhookControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    DiscordWebhookService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void GivenBlankUrl_WhenCreate_ThenRespondsWithBadRequestStatus() throws Exception {
        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                "",
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/v1/discord-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenEmptySchedulesIdsSet_WhenCreate_ThenRespondsWithConflictStatus() throws Exception {
        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                "http://https://discord.com/api/webhooks/123/123",
                Set.of()
        );

        String content = mapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/v1/discord-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenNotAvailableUrl_WhenCreate_ThenRespondsWithConflictStatus() throws Exception {
        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/123",
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        doThrow(DiscordWebhookUrlNotAvailableException.class)
                .when(service)
                .create(eq(payload), any());

        mockMvc.perform(post("/api/v1/discord-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict());
    }

    @Test
    void GivenValidPayload_WhenCreate_ThenRespondsWithCreatedStatusAndProperBody() throws Exception {
        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/123",
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("url")
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .type(0)
                                .planPolslId(1)
                                .semester(2)
                                .groupNumber(3)
                                .name("schedule-name-1")
                                .addedBy(user)
                                .build(),
                        Schedule.builder()
                                .id(2L)
                                .type(0)
                                .planPolslId(12)
                                .semester(2)
                                .groupNumber(3)
                                .name("schedule-name-2")
                                .addedBy(user)
                                .build()
                ))
                .addedBy(user)
                .build();

        when(service.create(eq(payload), any()))
                .thenReturn(new DiscordWebhookResponse(discordWebhook));

        mockMvc.perform(post("/api/v1/discord-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.id").value(1),

                        jsonPath("$.url").value("url"),

                        jsonPath("$.added_by").isNotEmpty(),
                        jsonPath("$.added_by.email").value("email@email.com"),
                        jsonPath("$.schedules").isNotEmpty(),

                        jsonPath("$.schedules[0].id").isNotEmpty(),
                        jsonPath("$.schedules[0].semester").isNotEmpty(),
                        jsonPath("$.schedules[0].name").isNotEmpty(),
                        jsonPath("$.schedules[0].group_number").isNotEmpty(),

                        jsonPath("$.schedules[1].id").isNotEmpty(),
                        jsonPath("$.schedules[1].semester").isNotEmpty(),
                        jsonPath("$.schedules[1].name").isNotEmpty(),
                        jsonPath("$.schedules[1].group_number").isNotEmpty()
                );
    }

    @Test
    void GivenNotExistingDiscordWebhookId_WhenDelete_ThenRespondsWithNotFoundStatus() throws Exception {
        doThrow(ResourceNotFoundException.class)
                .when(service)
                .delete(eq(1337L), any());

        mockMvc.perform(delete("/api/v1/discord-webhook/1337"))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenUserThatIsNotAdminNorOwnerOfDiscordWebhook_WhenDelete_ThenRespondsWithNotFoundStatus() throws Exception {
        doThrow(PermissionDeniedException.class)
                .when(service)
                .delete(eq(1L), any());

        mockMvc.perform(delete("/api/v1/discord-webhook/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenExistingDiscordWebhookIdAndItsOwner_WhenDelete_ThenRespondsWithOkStatusAndSuccessResponse() throws Exception {
        when(service.delete(eq(1L), any()))
                .thenReturn(new SuccessResponse());

        mockMvc.perform(delete("/api/v1/discord-webhook/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void GivenEmptySchedulesIdsSet_WhenUpdateDiscordWebhookSchedules_ThenRespondsWithBadRequestStatus() throws Exception {
        UpdateDiscordWebhookRequest payload = new UpdateDiscordWebhookRequest(
                Set.of()
        );

        String content = mapper.writeValueAsString(payload);

        mockMvc.perform(patch("/api/v1/discord-webhook/1337")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenNotExistingDiscordWebhookId_WhenUpdateDiscordWebhookSchedules_ThenRespondsWithNotFoundStatus() throws Exception {
        UpdateDiscordWebhookRequest payload = new UpdateDiscordWebhookRequest(
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        doThrow(ResourceNotFoundException.class)
                .when(service)
                .updateDiscordWebhookSchedules(eq(payload), eq(1337L), any());

        mockMvc.perform(patch("/api/v1/discord-webhook/1337")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenUserThatIsNotAdminNorOwnerOfDiscordWebhook_WhenUpdateDiscordWebhookSchedules_ThenRespondsWithForbiddenStatus() throws Exception {
        UpdateDiscordWebhookRequest payload = new UpdateDiscordWebhookRequest(
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        doThrow(PermissionDeniedException.class)
                .when(service)
                .updateDiscordWebhookSchedules(eq(payload), eq(1337L), any());

        mockMvc.perform(patch("/api/v1/discord-webhook/1337")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isForbidden());
    }

    @Test
    void GivenExistingDiscordWebhookIdAndItsOwner_WhenUpdateDiscordWebhookSchedules_ThenRespondsWithOkStatusAndSuccessResponse() throws Exception {
        UpdateDiscordWebhookRequest payload = new UpdateDiscordWebhookRequest(
                Set.of(1L)
        );

        String content = mapper.writeValueAsString(payload);

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("url")
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .type(0)
                                .planPolslId(1)
                                .semester(2)
                                .groupNumber(3)
                                .name("schedule-name-1")
                                .addedBy(user)
                                .build()
                ))
                .addedBy(user)
                .build();

        when(service.updateDiscordWebhookSchedules(eq(payload), eq(1L), any()))
                .thenReturn(new DiscordWebhookResponse(discordWebhook));

        mockMvc.perform(patch("/api/v1/discord-webhook/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(1),

                        jsonPath("$.url").value("url"),

                        jsonPath("$.added_by").isNotEmpty(),
                        jsonPath("$.added_by.email").value("email@email.com"),
                        jsonPath("$.schedules").isNotEmpty(),

                        jsonPath("$.schedules[0].id").isNotEmpty(),
                        jsonPath("$.schedules[0].semester").isNotEmpty(),
                        jsonPath("$.schedules[0].name").isNotEmpty(),
                        jsonPath("$.schedules[0].group_number").isNotEmpty()
                );
    }
}
