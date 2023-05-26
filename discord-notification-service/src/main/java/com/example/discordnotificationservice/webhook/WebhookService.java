package com.example.discordnotificationservice.webhook;

import com.example.discordnotificationservice.discord.DiscordWebhookService;
import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.schedule.ScheduleService;
import com.example.discordnotificationservice.security.SecurityService;
import com.example.discordnotificationservice.shared.exception.ForbiddenAccessException;
import com.example.discordnotificationservice.shared.exception.ResourceNotFoundException;
import com.example.discordnotificationservice.webhook.dto.WebhookRequest;
import com.example.discordnotificationservice.webhook.dto.WebhookResponse;
import com.example.discordnotificationservice.webhook.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.webhook.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.webhook.exception.UnavailableDiscordWebhookException;
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

    private static final int PAGE_SIZE = 10;

    @Transactional
    public WebhookResponse create(WebhookRequest request, Jwt jwt) {
        String discordWebhookUrl = request.url();

        if (discordWebhookService.isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new InvalidDiscordWebhookUrlException();
        }

        DiscordWebhook discordWebhook = discordWebhookService.getDiscordWebhookFromUrl(discordWebhookUrl);

        if (isDiscordWebhookUnavailable(discordWebhook, null)) {
            throw new UnavailableDiscordWebhookException();
        }

        Set<UUID> schedules = request.schedules();

        if (doesAnyScheduleDoNotExist(request.schedules())) {
            throw new NotExistingSchedulesException();
        }

        discordWebhookService.sendWelcomeMessage(discordWebhook);

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
            throw new InvalidDiscordWebhookUrlException();
        }

        DiscordWebhook discordWebhook = discordWebhookService.getDiscordWebhookFromUrl(discordWebhookUrl);

        if (isDiscordWebhookUnavailable(discordWebhook, webhook.getId())) {
            throw new UnavailableDiscordWebhookException();
        }

        Set<UUID> currentSchedules = webhook.getSchedules();
        Set<UUID> schedulesToCheck = request.schedules().stream()
                .filter(schedule -> !currentSchedules.contains(schedule))
                .collect(Collectors.toSet());

        if (doesAnyScheduleDoNotExist(schedulesToCheck)) {
            throw new NotExistingSchedulesException();
        }

        discordWebhookService.sendWelcomeMessage(discordWebhook);

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
}
