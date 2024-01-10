package com.github.karixdev.webhookservice.service;

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
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.webhookservice.matcher.DeepWebhookArgumentMatcher.deepWebhookEq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

		when(jwt.getSubject()).thenReturn(userId);

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

}