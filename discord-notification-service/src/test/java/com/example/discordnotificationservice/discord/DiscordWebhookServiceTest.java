package com.example.discordnotificationservice.discord;


import com.example.discordnotificationservice.discord.dto.DiscordMessageRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import com.example.discordnotificationservice.discord.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.discord.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.discord.exception.UnavailableDiscordApiIdException;
import com.example.discordnotificationservice.discord.exception.UnavailableTokenException;
import com.example.discordnotificationservice.schedule.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordWebhookServiceTest {
    @InjectMocks
    DiscordWebhookService underTest;

    @Mock
    ScheduleService scheduleService;

    @Mock
    DiscordWebhookRepository repository;

    @Mock
    DiscordApiWebhooksClient discordApiWebhooksClient;

    @Mock
    Jwt jwt;

    @Test
    void GivenInvalidDiscordWebhookUrl_WhenCreate_ThenThrowsInvalidDiscordWebhookUrlException() {
        // Given
        DiscordWebhookRequest request = new DiscordWebhookRequest("invalid url", Set.of());

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(InvalidDiscordWebhookUrlException.class)
                .hasMessage("Provided Discord webhook url is invalid");
    }

    @Test
    void GivenRequestContainingNotExistingSchedules_WhenCreate_ThenThrowsNotExistingSchedulesException() {
        // Given
        Set<UUID> schedules = Set.of(UUID.randomUUID());
        DiscordWebhookRequest request = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(false);

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(NotExistingSchedulesException.class)
                .hasMessage("Provided set of schedules includes non-existing schedules");
    }

    @Test
    void GivenUrlWithUnavailableDiscordApiId_WhenCreate_ThenThrowsUnavailableDiscordApiIdException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);

        DiscordWebhookRequest request = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id("111-222-333")
                .discordApiId("123")
                .token("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.of(discordWebhook));

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(UnavailableDiscordApiIdException.class)
                .hasMessage("Id in provided url is unavailable");
    }

    @Test
    void GivenUrlWithUnavailableToken_WhenCreate_ThenThrowsUnavailableTokenException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);

        DiscordWebhookRequest request = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        DiscordWebhook discordWebhook = DiscordWebhook.builder()
                .id("111-222-333")
                .discordApiId("456")
                .token("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.of(discordWebhook));

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(UnavailableTokenException.class)
                .hasMessage("Token in provided url is unavailable");
    }

    @Test
    void GivenJwtAndValidRequest_WhenCreate_ThenSendWelcomeMessageSavesWebhookAndReturnsCorrectResponse() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);

        DiscordWebhookRequest request = new DiscordWebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        String userId = "123-456-789";

        when(jwt.getSubject()).thenReturn(userId);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(true);

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.empty());

        DiscordWebhook savedDiscordWebhook = DiscordWebhook.builder()
                .id("111-222-333")
                .discordApiId("123")
                .token("abc")
                .addedBy(userId)
                .schedules(schedules)
                .build();

        when(repository.save(eq(
                DiscordWebhook.builder()
                        .discordApiId("123")
                        .token("abc")
                        .addedBy(userId)
                        .schedules(schedules)
                        .build())))
                .thenReturn(savedDiscordWebhook);

        DiscordWebhookResponse expected = new DiscordWebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        // When
        DiscordWebhookResponse result = underTest.create(request, jwt);

        // Then
        verify(discordApiWebhooksClient).sendMessage(
                eq("123"),
                eq("abc"),
                eq(new DiscordMessageRequest("Hello form PolslCourseApi!"))
        );

        assertThat(result).isEqualTo(expected);
    }
}