package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.exception.ForbiddenAccessException;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.webhookservice.document.Webhook;
import com.github.karixdev.webhookservice.dto.WebhookRequest;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
import com.github.karixdev.webhookservice.exception.CollectionContainingNotExistingScheduleException;
import com.github.karixdev.webhookservice.exception.NotExistingDiscordWebhookException;
import com.github.karixdev.webhookservice.exception.UnavailableDiscordWebhookUrlException;
import com.github.karixdev.webhookservice.mapper.WebhookMapper;
import com.github.karixdev.webhookservice.model.DiscordWebhookParameters;
import com.github.karixdev.webhookservice.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

	private final WebhookRepository repository;
	private final WebhookMapper mapper;
	private final DiscordWebhookService discordWebhookService;
	private final ScheduleService scheduleService;
	private final SecurityService securityService;
	private final PaginationService paginationService;

	@Transactional
	public WebhookResponse create(WebhookRequest request, Jwt jwt) {
		String discordWebhookUrl = request.discordWebhookUrl();
		if (repository.findByDiscordWebhookUrl(discordWebhookUrl).isPresent()) {
			throw new UnavailableDiscordWebhookUrlException();
		}

		Set<UUID> schedulesIds = request.schedulesIds();

		CompletableFuture<Boolean> doesWebhookExistTask = createDoesWebhookExistTask(discordWebhookUrl, true);
		CompletableFuture<Boolean> doSchedulesExistTask = createDoSchedulesExistTask(schedulesIds);

		CompletableFuture.allOf(doesWebhookExistTask, doSchedulesExistTask).join();

		validateExistenceOfDiscordWebhook(doesWebhookExistTask);
		validateExistenceOfSchedules(doSchedulesExistTask);

		Webhook webhook = Webhook.builder()
				.addedBy(securityService.getUserId(jwt))
				.schedulesIds(schedulesIds)
				.discordWebhookUrl(discordWebhookUrl)
				.build();

		repository.save(webhook);

		return mapper.mapToResponse(webhook);
	}

	public Page<WebhookResponse> findAll(Jwt jwt, Integer page, Integer pageSize) {
		PageRequest pageRequest = paginationService.getPageRequest(page, pageSize);

		String userId = securityService.getUserId(jwt);

		Page<Webhook> webhooks = securityService.isAdmin(jwt)
				? repository.findAll(pageRequest)
				: repository.findByAddedBy(userId, pageRequest);


		return mapper.mapToResponsePage(webhooks);
	}

	@Transactional
	public WebhookResponse update(String id, WebhookRequest request, Jwt jwt) {
		Webhook webhook = findByIdOrElseThrow(id);
		validateIfUserCanAccessWebhook(webhook, jwt);

		String discordWebhookUrl = request.discordWebhookUrl();
		boolean isDiscordWebhookUrlNew = !discordWebhookUrl.equals(webhook.getDiscordWebhookUrl());

		if (isDiscordWebhookUrlNew && repository.findByDiscordWebhookUrl(discordWebhookUrl).isPresent()) {
			throw new UnavailableDiscordWebhookUrlException();
		}

		Set<UUID> schedulesIds = request.schedulesIds();
		Set<UUID> newSchedulesIds = schedulesIds.stream()
				.filter(scheduleId -> !webhook.getSchedulesIds().contains(scheduleId))
				.collect(Collectors.toSet());


		CompletableFuture<Boolean> doesWebhookExistTask = createDoesWebhookExistTask(discordWebhookUrl, isDiscordWebhookUrlNew);
		CompletableFuture<Boolean> doSchedulesExistTask = createDoSchedulesExistTask(newSchedulesIds);

		CompletableFuture.allOf(doesWebhookExistTask, doSchedulesExistTask).join();

		validateExistenceOfDiscordWebhook(doesWebhookExistTask);
		validateExistenceOfSchedules(doSchedulesExistTask);

		webhook.setDiscordWebhookUrl(discordWebhookUrl);
		webhook.setSchedulesIds(schedulesIds);

		if (securityService.isAdmin(jwt) && request.addedBy() != null) {
			webhook.setAddedBy(request.addedBy());
		}

		repository.save(webhook);

		return mapper.mapToResponse(webhook);
	}

	@Transactional
	public void delete(String id, Jwt jwt) {
		Webhook webhook = findByIdOrElseThrow(id);
		validateIfUserCanAccessWebhook(webhook, jwt);

		repository.delete(webhook);
	}

	private CompletableFuture<Boolean> createDoesWebhookExistTask(String discordWebhookUrl, boolean isDiscordWebhookUrlNew) {
		if (!isDiscordWebhookUrlNew) {
			return CompletableFuture.supplyAsync(() -> true);
		}

		DiscordWebhookParameters parameters = discordWebhookService.getParametersFromUrl(discordWebhookUrl);

		return CompletableFuture.supplyAsync(() -> discordWebhookService.doesWebhookExist(parameters.id(), parameters.token()));
	}

	private CompletableFuture<Boolean> createDoSchedulesExistTask(Set<UUID> schedulesIds) {
		if (schedulesIds.isEmpty()) {
			return CompletableFuture.supplyAsync(() -> true);
		}

		return CompletableFuture.supplyAsync(() -> scheduleService.doSchedulesExist(schedulesIds));
	}

	private Webhook findByIdOrElseThrow(String id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Webhook with provided id not found"));
	}

	private void validateIfUserCanAccessWebhook(Webhook webhook, Jwt jwt) {
		boolean isOwner = webhook.getAddedBy().equals(securityService.getUserId(jwt));
		boolean isAdmin = securityService.isAdmin(jwt);

		if (isOwner || isAdmin) {
			return;
		}

		throw new ForbiddenAccessException("You are not owner of webhook with provided id");
	}

	private void validateExistenceOfDiscordWebhook(CompletableFuture<Boolean> task) {
		if (Boolean.FALSE.equals(task.join())) {
			throw new NotExistingDiscordWebhookException();
		}
	}

	private void validateExistenceOfSchedules(CompletableFuture<Boolean> task) {
		if (Boolean.FALSE.equals(task.join())) {
			throw new CollectionContainingNotExistingScheduleException();
		}
	}

}
