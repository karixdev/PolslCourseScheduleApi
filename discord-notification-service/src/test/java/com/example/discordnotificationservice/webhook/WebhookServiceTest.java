package com.example.discordnotificationservice.webhook;


import com.example.discordnotificationservice.webhook.dto.DiscordMessageRequest;
import com.example.discordnotificationservice.webhook.dto.WebhookRequest;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import com.example.discordnotificationservice.webhook.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.webhook.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.webhook.exception.UnavailableDiscordApiIdException;
import com.example.discordnotificationservice.webhook.exception.UnavailableTokenException;
import com.example.discordnotificationservice.schedule.ScheduleService;
import com.example.discordnotificationservice.security.SecurityService;
import com.example.discordnotificationservice.shared.exception.ForbiddenAccessException;
import com.example.discordnotificationservice.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {
    @InjectMocks
    WebhookService underTest;

    @Mock
    ScheduleService scheduleService;

    @Mock
    WebhookRepository repository;

    @Mock
    DiscordApiWebhooksClient discordApiWebhooksClient;

    @Mock
    SecurityService securityService;

    @Mock
    WebhookDTOMapper mapper;

    @Mock
    Jwt jwt;

    @Test
    void GivenInvalidWebhookUrl_WhenCreate_ThenThrowsInvalidDiscordWebhookUrlException() {
        // Given
        WebhookRequest request = new WebhookRequest("invalid url", Set.of());

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(InvalidDiscordWebhookUrlException.class)
                .hasMessage("Provided Discord webhook url is invalid");
    }

    @Test
    void GivenRequestContainingNotExistingSchedules_WhenCreate_ThenThrowsNotExistingSchedulesException() {
        // Given
        Set<UUID> schedules = Set.of(UUID.randomUUID());
        WebhookRequest request = new WebhookRequest(
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
    void GivenUrlWithUnavailableDiscordId_WhenCreate_ThenThrowsUnavailableDiscordApiIdException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);

        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.of(webhook));

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

        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("456")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.of(webhook));

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

        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        String userId = "123-456-789";

        when(jwt.getSubject()).thenReturn(userId);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(true);

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.empty());

        Webhook savedWebhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy(userId)
                .schedules(schedules)
                .build();

        when(repository.save(eq(
                Webhook.builder()
                        .discordId("123")
                        .discordToken("abc")
                        .addedBy(userId)
                        .schedules(schedules)
                        .build())))
                .thenReturn(savedWebhook);

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        // When
        WebhookResponse result = underTest.create(request, jwt);

        // Then
        verify(discordApiWebhooksClient).sendMessage(
                eq("123"),
                eq("abc"),
                eq(new DiscordMessageRequest("Hello form PolslCourseApi!"))
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void GivenAdminJwt_WhenFindAll_ThenReturnsWebhooksResponsesPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        Set<UUID> schedules = Set.of(UUID.randomUUID());

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        PageImpl<Webhook> page =
                new PageImpl<>(List.of(webhook));

        when(securityService.isAdmin(jwt))
                .thenReturn(true);

        when(repository.findAll(pageRequest))
                .thenReturn(page);

        when(mapper.map(eq(webhook)))
                .thenReturn(expected);

        // When
        Page<WebhookResponse> result =
                underTest.findAll(jwt, 0);

        // Then
        assertThat(result.getContent())
                .isEqualTo(List.of(expected));
    }

    @Test
    void GivenUserJwt_WhenFindAll_ThenReturnsWebhookResponsesPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        Set<UUID> schedules = Set.of(UUID.randomUUID());
        String userId = "userId";

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy(userId)
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        PageImpl<Webhook> page =
                new PageImpl<>(List.of(webhook));

        when(securityService.getUserId(eq(jwt)))
                .thenReturn(userId);

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(false);

        when(repository.findByAddedBy(eq(userId), eq(pageRequest)))
                .thenReturn(page);

        when(mapper.map(eq(webhook)))
                .thenReturn(expected);

        // When
        Page<WebhookResponse> result =
                underTest.findAll(jwt, 0);

        // Then
        assertThat(result.getContent())
                .isEqualTo(List.of(expected));
    }

    @Test
    void GivenNotExistingId_WhenDelete_ThenThrowsResourceNotFoundException() {
        // Given
        String id = "id";

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id, jwt))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook with id %s not found".formatted(id));
    }

    @Test
    void GivenUserWhoDoesNotOwnWebhook_WhenDelete_ThenThrowsForbiddenAccessException() {
        // Given
        String id = "111-222-333";

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("otherUserId");

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id, jwt))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void GivenAdminWhoDoesNotOwnWebhook_WhenDelete_ThenDeletesWebhook() {
        // Given
        String id = "111-222-333";

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("otherUserId");

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(true);

        // When
        underTest.delete(id, jwt);

        // Then
        verify(repository).delete(eq(webhook));
    }

    @Test
    void GivenUserWhoOwnsWebhook_WhenDelete_ThenDeletesWebhook() {
        // Given
        String id = "111-222-333";

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("userId");

        // When
        underTest.delete(id, jwt);

        // Then
        verify(repository).delete(eq(webhook));
    }

    @Test
    void GivenNotExistingId_WhenUpdate_ThenThrowsResourceNotFoundException() {
        // Given
        WebhookRequest request = new WebhookRequest("invalid url", Set.of());
        String id = "id";

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook with id %s not found".formatted(id));
    }

    @Test
    void GivenUserWhoDoesNotOwnWebhook_WhenUpdate_ThenThrowsForbiddenAccessException() {
        // Given
        String id = "111-222-333";
        WebhookRequest request = new WebhookRequest("invalid url", Set.of());

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .discordId("123")
                .discordToken("abc")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("otherUserId");

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void GivenInvalidWebhookUrl_WhenUpdate_ThenThrowsInvalidWebhookUrlException() {
        // Given
        WebhookRequest request = new WebhookRequest("invalid url", Set.of());
        String id = "id";

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(InvalidDiscordWebhookUrlException.class)
                .hasMessage("Provided Discord webhook url is invalid");
    }

    @Test
    void GivenUrlWithUnavailableDiscordId_WhenUpdate_ThenThrowsUnavailableDiscordApiIdException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String id = "id";

        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.of(webhook));

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(UnavailableDiscordApiIdException.class)
                .hasMessage("Id in provided url is unavailable");
    }

    @Test
    void GivenUrlWithUnavailableToken_WhenUpdate_ThenThrowsUnavailableTokenException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String id = "id";

        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.of(webhook));

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(UnavailableTokenException.class)
                .hasMessage("Token in provided url is unavailable");
    }

    @Test
    void GivenRequestContainingNotExistingSchedules_WhenUpdate_ThenThrowsNotExistingSchedulesException() {
        // Given
        UUID newSchedule = UUID.fromString("57f6874d-1f91-4862-a31c-dfb8bea8ca72");

        Set<UUID> schedules = Set.of(
                UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442"),
                newSchedule
        );
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/123/abc", schedules);

        String id = "id";

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(Set.of(UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442")))
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(false);

        when(repository.findByDiscordApiId(eq("123")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abc")))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(NotExistingSchedulesException.class)
                .hasMessage("Provided set of schedules includes non-existing schedules");
    }

    @Test
    void GivenWebhookRequestAndAdminWhoIsNotOwner_WhenUpdate_ThenSendWelcomeMessageUpdatesWebhookAndReturnsCorrectResponse() {
        // Given
        UUID oldSchedule = UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442");
        UUID newSchedule = UUID.fromString("57f6874d-1f91-4862-a31c-dfb8bea8ca72");

        Set<UUID> schedules = Set.of(
                oldSchedule,
                newSchedule
        );
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/1234/abcd", schedules);

        String id = "id";

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(Set.of(oldSchedule))
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789-1");

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(true);

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(true);

        when(repository.findByDiscordApiId(eq("1234")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abcd")))
                .thenReturn(Optional.empty());

        Webhook expectedToBeSaved = Webhook.builder()
                .id(id)
                .discordId("1234")
                .discordToken("abcd")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.save(eq(expectedToBeSaved)))
                .thenReturn(expectedToBeSaved);

        WebhookResponse expectedResponse = new WebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        when(mapper.map(eq(expectedToBeSaved)))
                .thenReturn(expectedResponse);

        // When
        WebhookResponse result = underTest.update(request, jwt, id);

        // Then
        verify(discordApiWebhooksClient).sendMessage(
                eq("1234"),
                eq("abcd"),
                eq(new DiscordMessageRequest("Hello form PolslCourseApi!"))
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void GivenWebhookRequestAndUserWhoIsOwner_WhenUpdate_ThenSendWelcomeMessageUpdatesWebhookAndReturnsCorrectResponse() {
        // Given
        UUID oldSchedule = UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442");
        UUID newSchedule = UUID.fromString("57f6874d-1f91-4862-a31c-dfb8bea8ca72");

        Set<UUID> schedules = Set.of(
                oldSchedule,
                newSchedule
        );
        WebhookRequest request = new WebhookRequest(
                "https://discord.com/api/webhooks/1234/abcd", schedules);

        String id = "id";

        Webhook webhook = Webhook.builder()
                .id(id)
                .discordId("123")
                .discordToken("abc")
                .addedBy("123-456-789")
                .schedules(Set.of(oldSchedule))
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(true);

        when(repository.findByDiscordApiId(eq("1234")))
                .thenReturn(Optional.empty());

        when(repository.findByToken(eq("abcd")))
                .thenReturn(Optional.empty());

        Webhook expectedToBeSaved = Webhook.builder()
                .id(id)
                .discordId("1234")
                .discordToken("abcd")
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(repository.save(eq(expectedToBeSaved)))
                .thenReturn(expectedToBeSaved);

        WebhookResponse expectedResponse = new WebhookResponse(
                "111-222-333",
                "123",
                "abc",
                schedules
        );

        when(mapper.map(eq(expectedToBeSaved)))
                .thenReturn(expectedResponse);

        // When
        WebhookResponse result = underTest.update(request, jwt, id);

        // Then
        verify(discordApiWebhooksClient).sendMessage(
                eq("1234"),
                eq("abcd"),
                eq(new DiscordMessageRequest("Hello form PolslCourseApi!"))
        );

        assertThat(result).isEqualTo(expectedResponse);
    }
}