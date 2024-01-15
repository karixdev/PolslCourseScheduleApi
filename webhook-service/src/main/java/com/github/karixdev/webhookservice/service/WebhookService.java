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

	private static final String DISCORD_WEBHOOK_URl_FIELD_NAME = "discordWebhookUrl";
	private static final String SCHEDULES_IDS_URl_FIELD_NAME = "discordWebhookUrl";

	@Transactional
	public WebhookResponse create(WebhookRequest request, Jwt jwt) {
		if (repository.findByDiscordWebhookUrl(request.discordWebhookUrl()).isPresent()) {
			throw new UnavailableDiscordWebhookUrlException(DISCORD_WEBHOOK_URl_FIELD_NAME);
		}

		DiscordWebhookParameters parameters = discordWebhookService.getParametersFromUrl(request.discordWebhookUrl());

		CompletableFuture<Boolean> doesWebhookExistTask =
				CompletableFuture.supplyAsync(() -> discordWebhookService.doesWebhookExist(parameters.id(), parameters.token()));
		CompletableFuture<Boolean> doSchedulesExistTask =
				CompletableFuture.supplyAsync(() -> scheduleService.doSchedulesExist(request.schedulesIds()));

		CompletableFuture.allOf(doesWebhookExistTask, doSchedulesExistTask).join();

		validateExistenceOfDiscordWebhook(doesWebhookExistTask);
		validateExistenceOfSchedules(doSchedulesExistTask);

		Webhook webhook = Webhook.builder()
				.addedBy(jwt.getSubject())
				.schedulesIds(request.schedulesIds())
				.discordWebhookUrl(request.discordWebhookUrl())
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
			throw new UnavailableDiscordWebhookUrlException(DISCORD_WEBHOOK_URl_FIELD_NAME);
		}

		Set<UUID> newSchedulesIds = request.schedulesIds().stream()
				.filter(scheduleId -> !webhook.getSchedulesIds().contains(scheduleId))
				.collect(Collectors.toSet());

		DiscordWebhookParameters parameters = discordWebhookService.getParametersFromUrl(discordWebhookUrl);

		CompletableFuture<Boolean> doesWebhookExistTask =
				CompletableFuture.supplyAsync(() ->
						!isDiscordWebhookUrlNew || discordWebhookService.doesWebhookExist(parameters.id(), parameters.token()));

		CompletableFuture<Boolean> doSchedulesExistTask =
				CompletableFuture.supplyAsync(() ->
						newSchedulesIds.isEmpty() || scheduleService.doSchedulesExist(newSchedulesIds));

		CompletableFuture.allOf(doesWebhookExistTask, doSchedulesExistTask).join();

		validateExistenceOfDiscordWebhook(doesWebhookExistTask);
		validateExistenceOfSchedules(doSchedulesExistTask);

		webhook.setDiscordWebhookUrl(discordWebhookUrl);
		webhook.setSchedulesIds(request.schedulesIds());

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
			throw new NotExistingDiscordWebhookException(DISCORD_WEBHOOK_URl_FIELD_NAME);
		}
	}

	private void validateExistenceOfSchedules(CompletableFuture<Boolean> task) {
		if (Boolean.FALSE.equals(task.join())) {
			throw new CollectionContainingNotExistingScheduleException(SCHEDULES_IDS_URl_FIELD_NAME);
		}
	}

}
