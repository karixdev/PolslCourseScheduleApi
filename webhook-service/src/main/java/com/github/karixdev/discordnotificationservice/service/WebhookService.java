package com.github.karixdev.discordnotificationservice.service;

import com.github.karixdev.discordnotificationservice.client.NotificationServiceClient;
import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import com.github.karixdev.discordnotificationservice.document.Webhook;
import com.github.karixdev.discordnotificationservice.dto.WebhookRequest;
import com.github.karixdev.discordnotificationservice.dto.WebhookResponse;
import com.github.karixdev.discordnotificationservice.exception.ForbiddenAccessException;
import com.github.karixdev.discordnotificationservice.exception.ResourceNotFoundException;
import com.github.karixdev.discordnotificationservice.exception.ValidationException;
import com.github.karixdev.discordnotificationservice.exception.client.ServiceClientException;
import com.github.karixdev.discordnotificationservice.mapper.WebhookDTOMapper;
import com.github.karixdev.discordnotificationservice.message.CoursesUpdateMessage;
import com.github.karixdev.discordnotificationservice.producer.NotificationProducer;
import com.github.karixdev.discordnotificationservice.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final ScheduleService scheduleService;
    private final WebhookRepository repository;
    private final SecurityService securityService;
    private final WebhookDTOMapper mapper;
    private final DiscordWebhookService discordWebhookService;
    private final NotificationServiceClient notificationServiceClient;
    private final NotificationProducer notificationProducer;

    private static final int PAGE_SIZE = 10;

    @Transactional
    public WebhookResponse create(WebhookRequest request, Jwt jwt) {
        String discordWebhookUrl = request.url();

        if (discordWebhookService.isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new ValidationException(
                    "schedules",
                    "Provided Discord webhook url is invalid"
            );
        }

        DiscordWebhook discordWebhook = discordWebhookService.getDiscordWebhookFromUrl(discordWebhookUrl);

        if (isDiscordWebhookUnavailable(discordWebhook, null)) {
            throw new ValidationException(
                    "url",
                    "Provided Discord webhook is not available"
            );
        }

        Set<UUID> schedules = request.schedules();

        if (doesAnyScheduleDoNotExist(request.schedules())) {
            throw new ValidationException(
                    "schedules",
                    "Provided set of schedules includes non-existing schedules"
            );
        }

        try {
            notificationServiceClient.sendWelcomeMessage(
                    discordWebhook.getDiscordId(),
                    discordWebhook.getToken()
            );
        } catch (ServiceClientException e) {
            throw new ValidationException(
                    "url",
                    "Discord webhook url does not work"
            );
        }

        Webhook webhook = repository.save(
                Webhook.builder()
                        .discordWebhook(discordWebhook)
                        .addedBy(jwt.getSubject())
                        .schedules(schedules)
                        .build()
        );

        return mapper.map(webhook);
    }

    public Page<WebhookResponse> findAll(Jwt jwt, Integer page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

        String userId = securityService.getUserId(jwt);

        Page<Webhook> discordWebhooks = securityService.isAdmin(jwt) ?
                repository.findAll(pageRequest) :
                repository.findByAddedBy(userId, pageRequest);

        return discordWebhooks.map(mapper::map);
    }

    private Webhook findByIdOrElseThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Webhook with id %s not found".formatted(id)));
    }

    @Transactional
    public void delete(String id, Jwt jwt) {
        Webhook discordWebhook = findByIdOrElseThrow(id);

        String userId = securityService.getUserId(jwt);

        if (!discordWebhook.getAddedBy().equals(userId) && !securityService.isAdmin(jwt)) {
            throw new ForbiddenAccessException();
        }

        repository.delete(discordWebhook);
    }

    @Transactional
    public WebhookResponse update(WebhookRequest request, Jwt jwt, String id) {
        Webhook webhook = findByIdOrElseThrow(id);

        String userId = securityService.getUserId(jwt);

        if (!webhook.getAddedBy().equals(userId) && !securityService.isAdmin(jwt)) {
            throw new ForbiddenAccessException();
        }

        String discordWebhookUrl = request.url();

        if (discordWebhookService.isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new ValidationException(
                    "url",
                    "Provided Discord webhook url is invalid"
            );
        }

        DiscordWebhook discordWebhook = discordWebhookService.getDiscordWebhookFromUrl(discordWebhookUrl);

        if (isDiscordWebhookUnavailable(discordWebhook, webhook.getId())) {
            throw new ValidationException(
                    "url",
                    "Provided Discord webhook is not available"
            );
        }

        Set<UUID> currentSchedules = webhook.getSchedules();
        Set<UUID> schedulesToCheck = request.schedules().stream()
                .filter(schedule -> !currentSchedules.contains(schedule))
                .collect(Collectors.toSet());

        if (doesAnyScheduleDoNotExist(schedulesToCheck)) {
            throw new ValidationException(
                    "schedules",
                    "Provided set of schedules includes non-existing schedules"
            );
        }

        try {
            notificationServiceClient.sendWelcomeMessage(
                    discordWebhook.getDiscordId(),
                    discordWebhook.getToken()
            );
        } catch (ServiceClientException e) {
            throw new ValidationException(
                    "url",
                    "Discord webhook url does not work"
            );
        }

        webhook.setDiscordWebhook(discordWebhook);
        webhook.setSchedules(request.schedules());

        webhook = repository.save(webhook);

        return mapper.map(webhook);
    }

    private boolean isDiscordWebhookUnavailable(DiscordWebhook discordWebhook, String id) {
        Optional<Webhook> optionalWebhook = repository.findByDiscordWebhook(discordWebhook);
        return optionalWebhook.isPresent() && !optionalWebhook.get().getId().equals(id);
    }

    private boolean doesAnyScheduleDoNotExist(Set<UUID> schedules) {
        return !scheduleService.checkIfSchedulesExist(schedules);
    }

    public List<Webhook> findBySchedule(UUID schedule) {
        return repository.findBySchedulesContaining(schedule);
    }

    public void handleCourseUpdateMessage(CoursesUpdateMessage message) {
        String scheduleName = scheduleService.getScheduleName(message.scheduleId());

        findBySchedule(message.scheduleId()).stream()
                .map(Webhook::getDiscordWebhook)
                .forEach(discordWebhook ->
                        notificationProducer.produceNotificationMessage(
                                scheduleName,
                                discordWebhook)
                );
    }
}
