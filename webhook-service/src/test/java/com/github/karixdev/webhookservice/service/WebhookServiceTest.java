package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.exception.ForbiddenAccessException;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.dto.WebhookRequest;
import com.github.karixdev.webhookservice.exception.CollectionContainingNotExistingScheduleException;
import com.github.karixdev.webhookservice.exception.NotExistingDiscordWebhookException;
import com.github.karixdev.webhookservice.exception.UnavailableDiscordWebhookUrlException;
import com.github.karixdev.webhookservice.mapper.WebhookMapper;
import com.github.karixdev.webhookservice.model.DiscordWebhookParameters;
import com.github.karixdev.webhookservice.repository.WebhookRepository;
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

import static com.github.karixdev.webhookservice.matcher.DeepWebhookArgumentMatcher.deepWebhookEq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

	@InjectMocks
	WebhookService underTest;

	@Mock
	WebhookRepository repository;

	@Mock
	WebhookMapper mapper;

	@Mock
	DiscordWebhookService discordWebhookService;

	@Mock
	ScheduleService scheduleService;

	@Mock
	SecurityService securityService;

	@Mock
	PaginationService paginationService;

	@Mock
	Jwt jwt;

	@Test
	void GivenUnavailableDiscordWebhookUrl_WhenCreate_ThenUnavailableDiscordWebhookUrlExceptionIsThrown() {
		// Given
		String url = "url";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl(url)
				.build();

		when(repository.findByDiscordWebhookUrl(url))
				.thenReturn(Optional.of(new Webhook()));

		// When & Then
		assertThatThrownBy(() -> underTest.create(request, jwt))
				.isInstanceOf(UnavailableDiscordWebhookUrlException.class);
	}

	@Test
	void GivenNotExistingDiscordWebhookUrl_WhenCreate_ThenNotExistingDiscordWebhookExceptionIsThrown() {
		// Given
		String url = "url";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url")
				.schedulesIds(Set.of(UUID.randomUUID()))
				.build();

		when(repository.findByDiscordWebhookUrl(url))
				.thenReturn(Optional.empty());

		when(discordWebhookService.getParametersFromUrl(url))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token"))
				.thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.create(request, jwt))
				.isInstanceOf(NotExistingDiscordWebhookException.class);
	}

	@Test
	void GivenSetContainingNotExistingSchedule_WhenCreate_ThenCollectionContainingNotExistingScheduleExceptionIsThrown() {
		// Given
		Set<UUID> schedulesIds = Set.of(UUID.randomUUID());
		String url = "url";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl(url)
				.schedulesIds(schedulesIds)
				.build();

		when(repository.findByDiscordWebhookUrl(url))
				.thenReturn(Optional.empty());

		when(discordWebhookService.getParametersFromUrl(url))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token"))
				.thenReturn(true);

		when(scheduleService.doSchedulesExist(schedulesIds))
				.thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.create(request, jwt))
				.isInstanceOf(CollectionContainingNotExistingScheduleException.class);
	}

	@Test
	void GivenValidRequestAndJwt_WhenCreate_ThenWebhookIsCreatedAndMappedToResponse() {
		// Given
		Set<UUID> schedulesIds = Set.of(UUID.randomUUID());
		String url = "url";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl(url)
				.schedulesIds(schedulesIds)
				.build();

		String userId = "userId";

		when(repository.findByDiscordWebhookUrl(url))
				.thenReturn(Optional.empty());

		when(discordWebhookService.getParametersFromUrl(url))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token"))
				.thenReturn(true);

		when(scheduleService.doSchedulesExist(schedulesIds))
				.thenReturn(true);

		when(securityService.getUserId(jwt)).thenReturn(userId);

		// When
		underTest.create(request, jwt);

		// Then
		Webhook createdWebhook = Webhook.builder()
				.addedBy(userId)
				.schedulesIds(schedulesIds)
				.discordWebhookUrl(url)
				.build();

		verify(repository).save(deepWebhookEq(createdWebhook));
		verify(mapper).mapToResponse(deepWebhookEq(createdWebhook));
	}

	@Test
	void GivenNormalUserJwtAndPageAndPageSize_WhenFindAll_ThenRetrievesWebhooksAddedByUserAndMapsIntoPageResponse() {
		// Given
		int page = 0;
		int pageSize = 10;

		PageRequest pageRequest = PageRequest.of(page, pageSize);

		String userId = "userId";

		Page<Webhook> pageImpl = new PageImpl<>(List.of(
				Webhook.builder()
						.id("id")
						.addedBy(userId)
						.schedulesIds(Set.of(UUID.randomUUID()))
						.discordWebhookUrl("url")
						.build()
		));

		when(paginationService.getPageRequest(page, pageSize))
				.thenReturn(pageRequest);

		when(securityService.getUserId(jwt))
				.thenReturn(userId);

		when(securityService.isAdmin(jwt))
				.thenReturn(false);

		when(repository.findByAddedBy(userId, pageRequest))
				.thenReturn(pageImpl);

		// When
		underTest.findAll(jwt, page, pageSize);

		// Then
		verify(repository).findByAddedBy(userId, pageRequest);
		verify(mapper).mapToResponsePage(pageImpl);
	}

	@Test
	void GivenNormalUserJwtAndPageAndPageSize_WhenFindAll_ThenRetrievesAllWebhooksAndMapsIntoPageResponse() {
		// Given
		int page = 0;
		int pageSize = 10;

		PageRequest pageRequest = PageRequest.of(page, pageSize);

		String userId = "userId";

		Page<Webhook> pageImpl = new PageImpl<>(List.of(
				Webhook.builder()
						.id("id")
						.addedBy(userId)
						.schedulesIds(Set.of(UUID.randomUUID()))
						.discordWebhookUrl("url")
						.build()
		));

		when(paginationService.getPageRequest(page, pageSize))
				.thenReturn(pageRequest);

		when(securityService.getUserId(jwt))
				.thenReturn(userId);

		when(securityService.isAdmin(jwt))
				.thenReturn(true);

		when(repository.findAll(pageRequest))
				.thenReturn(pageImpl);

		// When
		underTest.findAll(jwt, page, pageSize);

		// Then
		verify(repository).findAll(pageRequest);
		verify(mapper).mapToResponsePage(pageImpl);
	}

	@Test
	void GivenIdOfNotExistingWebhook_WhenUpdate_ThenThrowsResourceNotFoundException() {
		// Given
		String id = "id";
		WebhookRequest request = WebhookRequest.builder().build();

		when(repository.findById(id)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> underTest.update(id, request, jwt))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void GivenIdAndUsersWhoIsNotOwnerJwt_WhenUpdate_ThenThrowsForbiddenAccessException() {
		// Given
		String id = "id";
		WebhookRequest request = WebhookRequest.builder().build();

		when(repository.findById(id))
				.thenReturn(Optional.of(
						Webhook.builder()
								.id(id)
								.addedBy("otherUserId")
								.build()
				));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.update(id, request, jwt))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void GivenNewUrlThatIsAlreadyTakenByOtherWebhook_WhenUpdate_ThenThrowsUnavailableDiscordWebhookUrlException() {
		// Given
		String id = "id";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url-2")
				.build();

		when(repository.findById(id))
				.thenReturn(Optional.of(
						Webhook.builder()
								.id(id)
								.discordWebhookUrl("url-1")
								.addedBy("userId")
								.build()
				));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		when(repository.findByDiscordWebhookUrl("url-2"))
				.thenReturn(Optional.of(Webhook.builder().build()));

		// When & Then
		assertThatThrownBy(() -> underTest.update(id, request, jwt))
				.isInstanceOf(UnavailableDiscordWebhookUrlException.class);
	}

	@Test
	void GivenNewUrlOfNotExistingDiscordWebhook_WhenUpdate_ThenThrowsNotExistingDiscordWebhookException() {
		// Given
		String id = "id";
		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url")
				.schedulesIds(Set.of())
				.build();

		when(repository.findById(id))
				.thenReturn(Optional.of(
						Webhook.builder()
								.id(id)
								.discordWebhookUrl("url-x")
								.addedBy("userId")
								.build()
				));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		when(discordWebhookService.getParametersFromUrl("url"))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token")).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.update(id, request, jwt))
				.isInstanceOf(NotExistingDiscordWebhookException.class);
	}

	@Test
	void GivenSchedulesIdsContainingNotExistingSchedules_WhenUpdate_ThenThrowsCollectionContainingNotExistingScheduleException() {
		// Given
		String id = "id";
		UUID scheduleId = UUID.randomUUID();

		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url")
				.schedulesIds(Set.of(scheduleId))
				.build();

		when(repository.findById(id))
				.thenReturn(Optional.of(
						Webhook.builder()
								.id(id)
								.discordWebhookUrl("url")
								.addedBy("userId")
								.build()
				));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		when(scheduleService.doSchedulesExist(Set.of(scheduleId))).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.update(id, request, jwt))
				.isInstanceOf(CollectionContainingNotExistingScheduleException.class);
	}

	@Test
	void GivenIdOfOwnedWebhookAndRequestWithNotChangedUrlAndSchedulesIds_WhenUpdate_ThenNeverMakesHttpCallDoesNotChangeOwnerAndFinallyMapsIntoResponse() {
		// Given
		String id = "id";
		UUID scheduleId = UUID.randomUUID();

		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url")
				.schedulesIds(Set.of(scheduleId))
				.addedBy("otherId")
				.build();

		Webhook webhook = Webhook.builder()
				.id(id)
				.schedulesIds(Set.of(scheduleId))
				.discordWebhookUrl("url")
				.addedBy("userId")
				.build();

		when(repository.findById(id)).thenReturn(Optional.of(webhook));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		// When
		underTest.update(id, request, jwt);

		// Then
		verify(discordWebhookService, never()).doesWebhookExist(any(), any());
		verify(scheduleService, never()).doSchedulesExist(any());

		verify(mapper).mapToResponse(deepWebhookEq(webhook));
	}

	@Test
	void GivenIdOfOwnedWebhookAndRequestWithChangedUrlAndSchedulesIds_WhenUpdate_ThenMakesHttpCallDoesNotChangeOwnerAndFinallyMapsIntoResponse() {
		// Given
		String id = "id";
		UUID scheduleId1 = UUID.randomUUID();
		UUID scheduleId2 = UUID.randomUUID();

		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url-2")
				.schedulesIds(Set.of(scheduleId1, scheduleId2))
				.addedBy("otherId")
				.build();

		Webhook webhook = Webhook.builder()
				.id(id)
				.schedulesIds(Set.of(scheduleId1))
				.discordWebhookUrl("url")
				.addedBy("userId")
				.build();

		when(repository.findById(id)).thenReturn(Optional.of(webhook));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		when(repository.findByDiscordWebhookUrl("url-2")).thenReturn(Optional.empty());

		when(discordWebhookService.getParametersFromUrl("url-2"))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token")).thenReturn(true);
		when(scheduleService.doSchedulesExist(Set.of(scheduleId2))).thenReturn(true);

		// When
		underTest.update(id, request, jwt);

		// Then
		Webhook expectedWebhook = Webhook.builder()
				.id(id)
				.schedulesIds(Set.of(scheduleId1, scheduleId2))
				.discordWebhookUrl("url-2")
				.addedBy("userId")
				.build();

		verify(repository).save(deepWebhookEq(expectedWebhook));
		verify(mapper).mapToResponse(deepWebhookEq(expectedWebhook));
	}

	@Test
	void GivenIdOfNotOwnedByAdminWebhook_WhenUpdate_ThenAdminUpdatesWebhookAndCanChangeOwner() {
		// Given
		String id = "id";
		UUID scheduleId1 = UUID.randomUUID();
		UUID scheduleId2 = UUID.randomUUID();

		WebhookRequest request = WebhookRequest.builder()
				.discordWebhookUrl("url-2")
				.schedulesIds(Set.of(scheduleId1, scheduleId2))
				.addedBy("otherId2")
				.build();

		Webhook webhook = Webhook.builder()
				.id(id)
				.schedulesIds(Set.of(scheduleId1))
				.discordWebhookUrl("url")
				.addedBy("otherUserId")
				.build();

		when(repository.findById(id)).thenReturn(Optional.of(webhook));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(true);

		when(repository.findByDiscordWebhookUrl("url-2")).thenReturn(Optional.empty());

		when(discordWebhookService.getParametersFromUrl("url-2"))
				.thenReturn(new DiscordWebhookParameters("id", "token"));

		when(discordWebhookService.doesWebhookExist("id", "token")).thenReturn(true);
		when(scheduleService.doSchedulesExist(Set.of(scheduleId2))).thenReturn(true);

		// When
		underTest.update(id, request, jwt);

		// Then
		Webhook expectedWebhook = Webhook.builder()
				.id(id)
				.schedulesIds(Set.of(scheduleId1, scheduleId2))
				.discordWebhookUrl("url-2")
				.addedBy("otherId2")
				.build();

		verify(repository).save(deepWebhookEq(expectedWebhook));
		verify(mapper).mapToResponse(deepWebhookEq(expectedWebhook));
	}

	@Test
	void GivenIdOfNotExistingWebhook_WhenDelete_ThenThrowsResourceNotFoundException() {
		// Given
		String id = "id";

		when(repository.findById(id)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> underTest.delete(id, jwt))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void GivenUserJwtWhoIsNotOwner_WhenDelete_ThenThrowsForbiddenAccessException() {
		// Given
		String id = "id";

		when(repository.findById(id))
				.thenReturn(Optional.of(
						Webhook.builder()
								.id(id)
								.addedBy("otherUserId")
								.build()
				));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> underTest.delete(id, jwt))
				.isInstanceOf(ForbiddenAccessException.class);
	}

	@Test
	void GivenWebhookOwnerJwt_WhenDelete_ThenDeletesWebhook() {
		// Given
		String id = "id";

		Webhook webhook = Webhook.builder()
				.id(id)
				.addedBy("userId")
				.build();

		when(repository.findById(id)).thenReturn(Optional.of(webhook));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(false);

		// When
		underTest.delete(id, jwt);

		// Then
		verify(repository).delete(webhook);
	}

	@Test
	void GivenAdminJwtWhoIsNotOwnerOfWebhook_WhenDelete_ThenDeletesWebhook() {
		// Given
		String id = "id";

		Webhook webhook = Webhook.builder()
				.id(id)
				.addedBy("otherUserId")
				.build();

		when(repository.findById(id)).thenReturn(Optional.of(webhook));

		when(securityService.getUserId(jwt)).thenReturn("userId");
		when(securityService.isAdmin(jwt)).thenReturn(true);

		// When
		underTest.delete(id, jwt);

		// Then
		verify(repository).delete(webhook);
	}

}