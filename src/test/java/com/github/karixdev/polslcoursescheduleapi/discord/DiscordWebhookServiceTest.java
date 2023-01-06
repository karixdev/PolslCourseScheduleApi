package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookInvalidUrlException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebhookUrlNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.EmptySchedulesIdsSetException;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebhookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.UpdateDiscordWebhookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebhookResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.PermissionDeniedException;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.repsonse.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiscordWebhookServiceTest {
    @InjectMocks
    DiscordWebhookService underTest;

    @Mock
    DiscordWebhookRepository repository;

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
    void GivenPayloadWithInvalidUrl_WhenCreate_ThenThrowsDiscordWebhookInvalidUrlExceptionWithProperMessage() {
        // Given
        String url = "http://invalid.com/";

        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                url,
                Set.of(1L)
        );

        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebhookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        // When & Then
        assertThatThrownBy(() -> underTest.create(payload, userPrincipal))
                .isInstanceOf(DiscordWebhookInvalidUrlException.class)
                .hasMessage("Provided url is not valid");
    }

    @Test
    void GivenPayloadWithNotAvailableDiscordWebhookUrl_WhenCreate_ThenThrowsDiscordWebhookUrlNotAvailableExceptionWithProperMessage() {
        // Given
        String url = "https://discord.com/api/webhooks/not-available";

        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                url,
                Set.of(1L)
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebhookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        when(repository.findByUrl(eq(url)))
                .thenReturn(Optional.of(
                        DiscordWebhook.builder()
                                .url(url)
                                .schedules(Set.of(schedule))
                                .addedBy(user)
                                .build()
                ));

        // When & Then
        assertThatThrownBy(() -> underTest.create(payload, userPrincipal))
                .isInstanceOf(DiscordWebhookUrlNotAvailableException.class)
                .hasMessage("Discord web hook is not available");
    }

    @Test
    void GivenPayloadWithEmptySchedulesIdsSet_WhenCreate_ThenThrowsEmptySchedulesIdsSetExceptionWithProperMessage() {
        // Given
        String url = "https://discord.com/api/webhooks/available";

        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                url,
                Set.of()
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebhookBaseUrl())
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

        DiscordWebhookRequest payload = new DiscordWebhookRequest(
                url,
                Set.of(1L)
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(properties.getWebhookBaseUrl())
                .thenReturn("https://discord.com/api/webhooks/");

        when(repository.findByUrl(eq(url)))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(DiscordWebhook.builder()
                        .id(1L)
                        .url(url)
                        .schedules(Set.of(schedule))
                        .addedBy(user)
                        .build());

        when(scheduleService.getById(eq(1L)))
                .thenReturn(schedule);

        // When
        DiscordWebhookResponse result =
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

    @Test
    void GivenNotExistingDiscordWebhookId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithCorrectMessage() {
        // Given
        Long id = 1337L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id, new UserPrincipal(user)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Discord webhook with provided id not found");
    }

    @Test
    void GivenUserThatIsNotAdminNorOwnerOfDiscordWebhook_WhenDelete_ThenThrowsPermissionDeniedExceptionWithCorrectMessage() {
        // Given
        Long id = 1L;

        when(repository.findById(id))
                .thenReturn(Optional.of(
                        DiscordWebhook.builder()
                                .id(1L)
                                .url("http://discord.com/api")
                                .schedules(Set.of(schedule))
                                .addedBy(user)
                                .build()
                ));

        User otherUser = User.builder()
                .id(2L)
                .email("email-2@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build();

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id, new UserPrincipal(otherUser)))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("You are not the owner of the Discord webhook");
    }

    @Test
    void GivenAdminUserWhoIsNotTheOwnerOfDiscordWebhook_WhenDelete_ThenDeletesDiscordWebhook() {
        // Given
        Long id = 1L;

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("http://discord.com/api")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(discordWebhook));

        User otherUser = User.builder()
                .id(2L)
                .email("email-2@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        // When
        underTest.delete(id, new UserPrincipal(otherUser));

        // Then
        verify(repository).delete(eq(discordWebhook));
    }

    @Test
    void GivenOwnerOfDiscordWebhook_WhenDelete_ThenDeletesDiscordWebhook() {
        // Given
        Long id = 1L;

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("http://discord.com/api")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(discordWebhook));

        // When
        underTest.delete(id, new UserPrincipal(user));

        // Then
        verify(repository).delete(eq(discordWebhook));
    }

    @Test
    void GivenNotExistingDiscordWebhookId_WhenUpdateDiscordWebhookSchedules_ThenThrowsResourceNotFoundExceptionWithCorrectMessage() {
        // Given
        Long id = 1337L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                underTest.updateDiscordWebhookSchedules(
                        new UpdateDiscordWebhookRequest(),
                        id,
                        new UserPrincipal(user)
                ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Discord webhook with provided id not found");
    }

    @Test
    void GivenUserThatIsNotAdminNorOwnerOfDiscordWebhook_WhenUpdateDiscordWebhookSchedules_ThenThrowsPermissionDeniedExceptionWithCorrectMessage() {
        // Given
        Long id = 1L;

        when(repository.findById(id))
                .thenReturn(Optional.of(
                        DiscordWebhook.builder()
                                .id(1L)
                                .url("http://discord.com/api")
                                .schedules(Set.of(schedule))
                                .addedBy(user)
                                .build()
                ));

        User otherUser = User.builder()
                .id(2L)
                .email("email-2@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build();

        // When & Then
        assertThatThrownBy(() ->
                underTest.updateDiscordWebhookSchedules(
                        new UpdateDiscordWebhookRequest(),
                        id,
                        new UserPrincipal(otherUser)
                ))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("You are not the owner of the Discord webhook");
    }

    @Test
    void GivenAdminUserWhoIsNotTheOwnerOfDiscordWebhook_WhenUpdateDiscordWebhookSchedules_ThenThenReturnsCorrectResponse() {
        // Given
        Long id = 1L;

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("http://discord.com/api")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(discordWebhook));

        User otherUser = User.builder()
                .id(2L)
                .email("email-2@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        Schedule otherSchedule = Schedule.builder()
                .id(2L)
                .type(0)
                .planPolslId(11)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name-2")
                .addedBy(user)
                .build();

        when(scheduleService.getById(eq(otherSchedule.getId())))
                .thenReturn(otherSchedule);

        UpdateDiscordWebhookRequest payload =
                new UpdateDiscordWebhookRequest(Set.of(2L));

        discordWebhook.setSchedules(Set.of(otherSchedule));

        when(repository.save(any()))
                .thenReturn(discordWebhook);

        // When
        DiscordWebhookResponse result =
                underTest.updateDiscordWebhookSchedules(
                        payload,
                        id,
                        new UserPrincipal(otherUser));

        // Then
        assertThat(result.getSchedules()).isEqualTo(Set.of(
                new ScheduleResponse(otherSchedule)
        ));
    }

    @Test
    void GivenOwnerOfDiscordWebhook_WhenUpdateDiscordWebhookSchedules_ThenThenReturnsCorrectResponse() {
        // Given
        Long id = 1L;

        user.setUserRole(UserRole.ROLE_USER);

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id(1L)
                .url("http://discord.com/api")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(discordWebhook));

        Schedule otherSchedule = Schedule.builder()
                .id(2L)
                .type(0)
                .planPolslId(11)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name-2")
                .addedBy(user)
                .build();

        when(scheduleService.getById(eq(otherSchedule.getId())))
                .thenReturn(otherSchedule);

        UpdateDiscordWebhookRequest payload =
                new UpdateDiscordWebhookRequest(Set.of(2L));

        discordWebhook.setSchedules(Set.of(otherSchedule));

        when(repository.save(any()))
                .thenReturn(discordWebhook);

        // When
        DiscordWebhookResponse result =
                underTest.updateDiscordWebhookSchedules(
                        payload,
                        id,
                        new UserPrincipal(user));

        // Then
        assertThat(result.getSchedules()).isEqualTo(Set.of(
                new ScheduleResponse(otherSchedule)
        ));
    }

    @Test
    void GivenUserPrincipal_WhenGetUserDiscordWebhooks_ThenReturnsCorrectListOfDiscordWebhookResponse() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(repository.findByAddedBy(eq(userPrincipal.getUser())))
                .thenReturn(List.of(
                        DiscordWebhook.builder()
                                .id(1L)
                                .url("http://discord.com/api")
                                .schedules(Set.of(schedule))
                                .addedBy(user)
                                .build()
                ));

        // When
        List<DiscordWebhookResponse> result =
                underTest.getUserDiscordWebhooks(userPrincipal);

        // Then
        assertThat(result).hasSize(1);
    }
}
