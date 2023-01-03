package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebHookInvalidUrlException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebHookUrlNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.EmptySchedulesIdsSetException;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebHookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebHookResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.repsonse.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiscordWebHookServiceTest {
    @InjectMocks
    DiscordWebHookService underTest;

    @Mock
    DiscordWebHookRepository repository;

    @Mock
    ScheduleService scheduleService;

    @Mock
    DiscordApiService discordApiService;

    @Mock
    DiscordProperties properties;

    User user;

    Schedule schedule;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();
    }

    @Test
    void GivenPayloadWithInvalidUrl_WhenCreate_ThenThrowsDiscordWebHookInvalidUrlExceptionWithProperMessage() {
        // Given
        String url = "http://invalid.com/";

        DiscordWebHookRequest payload = new DiscordWebHookRequest(
                url,
                Set.of(1L)
        );

        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebHookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        // When & Then
        assertThatThrownBy(() -> underTest.create(payload, userPrincipal))
                .isInstanceOf(DiscordWebHookInvalidUrlException.class)
                .hasMessage("Provided url is not valid");
    }

    @Test
    void GivenPayloadWithNotAvailableDiscordWebHookUrl_WhenCreate_ThenThrowsDiscordWebHookUrlNotAvailableExceptionWithProperMessage() {
        // Given
        String url = "https://discord.com/api/webhooks/not-available";

        DiscordWebHookRequest payload = new DiscordWebHookRequest(
                url,
                Set.of(1L)
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebHookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        when(repository.findByUrl(eq(url)))
                .thenReturn(Optional.of(
                        DiscordWebHook.builder()
                                .url(url)
                                .schedules(Set.of(schedule))
                                .addedBy(user)
                                .build()
                ));

        // When & Then
        assertThatThrownBy(() -> underTest.create(payload, userPrincipal))
                .isInstanceOf(DiscordWebHookUrlNotAvailableException.class)
                .hasMessage("Discord web hook is not available");
    }

    @Test
    void GivenPayloadWithEmptySchedulesIdsSet_WhenCreate_ThenThrowsEmptySchedulesIdsSetExceptionWithProperMessage() {
        // Given
        String url = "https://discord.com/api/webhooks/available";

        DiscordWebHookRequest payload = new DiscordWebHookRequest(
                url,
                Set.of()
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebHookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        when(repository.findByUrl(eq(url)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.create(payload, userPrincipal))
                .isInstanceOf(EmptySchedulesIdsSetException.class)
                .hasMessage("Set with schedules ids is empty");
    }

    @Test
    void GivenValidPayload_WhenCreate_ThenReturnsCorrectResponseAndSendsWelcomeMessage() {
        // Given
        String url = "https://discord.com/api/webhooks/available";

        DiscordWebHookRequest payload = new DiscordWebHookRequest(
                url,
                Set.of(1L)
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebHookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        when(repository.findByUrl(eq(url)))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(DiscordWebHook.builder()
                        .id(1L)
                        .url(url)
                        .schedules(Set.of(schedule))
                        .addedBy(user)
                        .build());

        when(scheduleService.getById(eq(1L)))
                .thenReturn(schedule);

        // When
        DiscordWebHookResponse result =
                underTest.create(payload, userPrincipal);

        // Then
        verify(discordApiService).sendWelcomeMessage(eq(url));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUrl()).isEqualTo(url);

        assertThat(result.getAddedBy())
                .isEqualTo(new UserResponse(user));

        assertThat(result.getSchedules()).isEqualTo(Set.of(
                new ScheduleResponse(schedule)
        ));
    }
}
