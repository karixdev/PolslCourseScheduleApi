package com.example.discordnotificationservice.service;


import com.example.discordnotificationservice.client.NotificationServiceClient;
import com.example.discordnotificationservice.document.DiscordWebhook;
import com.example.discordnotificationservice.document.Webhook;
import com.example.discordnotificationservice.dto.WebhookRequest;
import com.example.discordnotificationservice.dto.WebhookResponse;
import com.example.discordnotificationservice.exception.*;
import com.example.discordnotificationservice.exception.client.ServiceClientException;
import com.example.discordnotificationservice.mapper.WebhookDTOMapper;
import com.example.discordnotificationservice.repository.WebhookRepository;
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
    SecurityService securityService;

    @Mock
    WebhookDTOMapper mapper;

    @Mock
    DiscordWebhookService discordWebhookService;

    @Mock
    NotificationServiceClient notificationServiceClient;

    @Mock
    Jwt jwt;

    @Test
    void GivenInvalidWebhookUrl_WhenCreate_ThenThrowsInvalidDiscordWebhookUrlException() {
        // Given
        String url = "invalid url";
        WebhookRequest request = new WebhookRequest(url, Set.of());

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(InvalidDiscordWebhookUrlException.class)
                .hasMessage("Provided Discord webhook url is invalid");
    }

    @Test
    void GivenRequestContainingNotExistingSchedules_WhenCreate_ThenThrowsNotExistingSchedulesException() {
        // Given
        Set<UUID> schedules = Set.of(UUID.randomUUID());
        String url = "https://discord.com/api/webhooks/123/abc";

        WebhookRequest request = new WebhookRequest(url, schedules);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(false);

        when(repository.findByDiscordWebhook(eq(new DiscordWebhook("123", "abc"))))
                .thenReturn(Optional.empty());

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(false);

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(new DiscordWebhook(
                        "123",
                        "abc"
                ));

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(NotExistingSchedulesException.class)
                .hasMessage("Provided set of schedules includes non-existing schedules");
    }

    @Test
    void GivenWebhookRequestWithUnavailableDiscordWebhookUrl_WhenCreate_ThenThrowsUnavailableDiscordWebhookException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String url = "https://discord.com/api/webhooks/456/def";

        WebhookRequest request = new WebhookRequest(url, schedules);

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .addedBy("123-456-789")
                .schedules(schedules)
                .discordWebhook(discordWebhook)
                .build();

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.of(webhook));

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(false);

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(UnavailableDiscordWebhookException.class)
                .hasMessage("Provided Discord webhook is not available");
    }

    @Test
    void GivenWebhookRequestWithNotWorkingUrl_WhenCreate_ThenThrowsValidationException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String url = "https://discord.com/api/webhooks/456/def";

        WebhookRequest request = new WebhookRequest(url, schedules);

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.empty());

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(false);

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(true);

        when(notificationServiceClient.sendWelcomeMessage(eq("456"), eq("def")))
                .thenThrow(ServiceClientException.class);

        // When & Then
        assertThatThrownBy(() -> underTest.create(request, jwt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Discord webhook url does not work");
    }

    @Test
    void GivenJwtAndValidRequest_WhenCreate_ThenSendWelcomeMessageSavesWebhookAndReturnsCorrectResponse() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String url = "https://discord.com/api/webhooks/123/abc";

        WebhookRequest request = new WebhookRequest(url, schedules);

        String userId = "123-456-789";

        when(jwt.getSubject()).thenReturn(userId);

        when(scheduleService.checkIfSchedulesExist(eq(schedules)))
                .thenReturn(true);

        DiscordWebhook discordWebhook = new DiscordWebhook("123", "abc");

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.empty());

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(false);

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        Webhook savedWebhook = Webhook.builder()
                .id("111-222-333")
                .addedBy(userId)
                .schedules(schedules)
                .discordWebhook(discordWebhook)
                .build();

        when(repository.save(eq(
                Webhook.builder()
                        .addedBy(userId)
                        .discordWebhook(discordWebhook)
                        .schedules(schedules)
                        .build())))
                .thenReturn(savedWebhook);

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "https://discord-webhook-url.com",
                schedules
        );

        when(mapper.map(eq(savedWebhook)))
                .thenReturn(expected);

        // When
        WebhookResponse result = underTest.create(request, jwt);

        // Then
        verify(notificationServiceClient).sendWelcomeMessage(eq("123"), eq("abc"));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void GivenAdminJwt_WhenFindAll_ThenReturnsWebhooksResponsesPage() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        Set<UUID> schedules = Set.of(UUID.randomUUID());

        Webhook webhook = Webhook.builder()
                .id("111-222-333")
                .addedBy("userId")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "https://discord-webhook-url.com",
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
                .addedBy(userId)
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        WebhookResponse expected = new WebhookResponse(
                "111-222-333",
                "https://discord-webhook-url.com",
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
        String url = "invalid url";
        WebhookRequest request = new WebhookRequest(url, Set.of());
        String id = "id";

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(Set.of(UUID.randomUUID()))
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(discordWebhookService.isNotValidDiscordWebhookUrl(eq(url)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(InvalidDiscordWebhookUrlException.class)
                .hasMessage("Provided Discord webhook url is invalid");
    }

    @Test
    void GivenWebhookRequestWithUnavailableDiscordWebhookUrl_WhenUpdate_ThenThrowsUnavailableDiscordWebhookException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<UUID> schedules = Set.of(scheduleId);
        String url = "https://discord.com/api/webhooks/456/def";

        String id = "id";
        WebhookRequest request = new WebhookRequest(url, schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(schedules)
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        Webhook otherWebhook = Webhook.builder()
                .id("otherId")
                .discordWebhook(discordWebhook)
                .addedBy("123-456-789")
                .schedules(schedules)
                .build();

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(otherWebhook));

        when(repository.findByDiscordWebhook(eq(new DiscordWebhook("456", "def"))))
                .thenReturn(Optional.of(webhook));

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(UnavailableDiscordWebhookException.class)
                .hasMessage("Provided Discord webhook is not available");
    }

    @Test
    void GivenRequestContainingNotExistingSchedules_WhenUpdate_ThenThrowsNotExistingSchedulesException() {
        // Given
        UUID newSchedule = UUID.fromString("57f6874d-1f91-4862-a31c-dfb8bea8ca72");
        Set<UUID> schedules = Set.of(
                UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442"),
                newSchedule
        );
        String url = "https://discord.com/api/webhooks/456/def";

        String id = "id";
        WebhookRequest request = new WebhookRequest(url, schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(Set.of(UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442")))
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(false);

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.of(webhook));

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(NotExistingSchedulesException.class)
                .hasMessage("Provided set of schedules includes non-existing schedules");
    }

    @Test
    void GivenWebhookRequestWithNotWorkingUrl_WhenUpdate_ThenThrowsValidationException() {
        // Given
        UUID newSchedule = UUID.fromString("57f6874d-1f91-4862-a31c-dfb8bea8ca72");
        Set<UUID> schedules = Set.of(
                UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442"),
                newSchedule
        );
        String url = "https://discord.com/api/webhooks/456/def";

        String id = "id";
        WebhookRequest request = new WebhookRequest(url, schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(Set.of(UUID.fromString("bb46a7d5-f267-4527-a00b-13c1172ac442")))
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(true);

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.of(webhook));

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        when(notificationServiceClient.sendWelcomeMessage(eq("456"), eq("def")))
                .thenThrow(ServiceClientException.class);

        // When & Then
        assertThatThrownBy(() -> underTest.update(request, jwt, id))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Discord webhook url does not work");
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
        String url = "https://discord.com/api/webhooks/456/def";

        String id = "id";
        WebhookRequest request = new WebhookRequest(url, schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(Set.of(oldSchedule))
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789-1");

        when(securityService.isAdmin(eq(jwt)))
                .thenReturn(true);

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(true);

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.empty());

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        Webhook expectedToBeSaved = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .discordWebhook(discordWebhook)
                .schedules(schedules)
                .build();

        when(repository.save(eq(expectedToBeSaved)))
                .thenReturn(expectedToBeSaved);

        WebhookResponse expectedResponse = new WebhookResponse(
                "111-222-333",
                "https://discord-webhook-url.com",
                schedules
        );

        when(mapper.map(eq(expectedToBeSaved)))
                .thenReturn(expectedResponse);

        // When
        WebhookResponse result = underTest.update(request, jwt, id);

        // Then
        verify(notificationServiceClient).sendWelcomeMessage(eq("456"), eq("def"));

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
        String url = "https://discord.com/api/webhooks/456/def";

        String id = "id";
        WebhookRequest request = new WebhookRequest(url, schedules);

        Webhook webhook = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .schedules(Set.of(oldSchedule))
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        DiscordWebhook discordWebhook = new DiscordWebhook("456", "def");

        when(securityService.getUserId(eq(jwt)))
                .thenReturn("123-456-789");

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(webhook));

        when(scheduleService.checkIfSchedulesExist(eq(Set.of(newSchedule))))
                .thenReturn(true);

        when(repository.findByDiscordWebhook(eq(discordWebhook)))
                .thenReturn(Optional.empty());

        when(discordWebhookService.getDiscordWebhookFromUrl(eq(url)))
                .thenReturn(discordWebhook);

        Webhook expectedToBeSaved = Webhook.builder()
                .id(id)
                .addedBy("123-456-789")
                .discordWebhook(discordWebhook)
                .schedules(schedules)
                .build();

        when(repository.save(eq(expectedToBeSaved)))
                .thenReturn(expectedToBeSaved);

        WebhookResponse expectedResponse = new WebhookResponse(
                "111-222-333",
                "https://discord-webhook-url.com",
                schedules
        );

        when(mapper.map(eq(expectedToBeSaved)))
                .thenReturn(expectedResponse);

        // When
        WebhookResponse result = underTest.update(request, jwt, id);

        // Then
        verify(notificationServiceClient).sendWelcomeMessage(eq("456"), eq("def"));

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void GivenSchedule_WhenFindBySchedule_ThenReturnsListOfWebhooks() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Webhook webhook = Webhook.builder()
                .id("123")
                .addedBy("123-456-789")
                .schedules(Set.of(scheduleId))
                .discordWebhook(new DiscordWebhook(
                        "123",
                        "abc"
                ))
                .build();

        when(repository.findBySchedulesContaining(eq(scheduleId)))
                .thenReturn(List.of(webhook));

        // When
        List<Webhook> result = underTest.findBySchedule(scheduleId);

        // Then
        assertThat(result).containsExactly(webhook);
    }
}