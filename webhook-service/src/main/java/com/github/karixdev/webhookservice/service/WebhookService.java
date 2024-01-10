package com.github.karixdev.webhookservice.service;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

	private final WebhookRepository repository;
	private final WebhookMapper mapper;
	private final DiscordWebhookService discordWebhookService;
	private final ScheduleService scheduleService;

	@Transactional
	public WebhookResponse create(WebhookRequest request, Jwt jwt) {
		if (repository.findByDiscordWebhookUrl(request.discordWebhookUrl()).isPresent()) {
			throw new UnavailableDiscordWebhookUrlException("discordWebhookUrl");
		}

		DiscordWebhookParameters parameters = discordWebhookService.getParametersFromUrl(request.discordWebhookUrl());

		CompletableFuture<Boolean> doesWebhookExistTask =
				CompletableFuture.supplyAsync(() -> discordWebhookService.doesWebhookExist(parameters.id(), parameters.token()));
		CompletableFuture<Boolean> doSchedulesExistTask =
				CompletableFuture.supplyAsync(() -> scheduleService.doSchedulesExist(request.schedulesIds()));

		CompletableFuture.allOf(doesWebhookExistTask, doSchedulesExistTask).join();

		boolean doesWebhookExist = doesWebhookExistTask.join();
		if (!doesWebhookExist) {
			throw new NotExistingDiscordWebhookException("discordWebhookUrl");
		}

		boolean doSchedulesExist = doSchedulesExistTask.join();
		if (!doSchedulesExist) {
			throw new CollectionContainingNotExistingScheduleException("schedulesIds");
		}

		Webhook webhook = Webhook.builder()
				.addedBy(jwt.getSubject())
				.schedulesIds(request.schedulesIds())
				.discordWebhookUrl(request.discordWebhookUrl())
				.build();

		repository.save(webhook);

		return mapper.mapToResponse(webhook);
	}

}
