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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final DiscordApiWebhooksClient discordApiWebhooksClient;
    private final ScheduleService scheduleService;
    private final WebhookRepository repository;
    private final SecurityService securityService;
    private final WebhookDTOMapper mapper;

    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";
    private static final String WELCOME_MESSAGE = "Hello form PolslCourseApi!";

    private static final int PAGE_SIZE = 10;

    @Transactional
    public WebhookResponse create(WebhookRequest request, Jwt jwt) {
        String discordWebhookUrl = request.url();

        if (isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new InvalidDiscordWebhookUrlException();
        }

        String discordApiId = getDiscordApiIdFromUrl(discordWebhookUrl);
        if (isDiscordApiIdUnavailable(discordApiId, null)) {
            throw new UnavailableDiscordApiIdException();
        }

        String token = getTokenFromUrl(discordWebhookUrl);
        if (isTokenUnavailable(token, null)) {
            throw new UnavailableTokenException();
        }

        Set<UUID> schedules = request.schedules();

        if (doesAnyScheduleDoNotExist(request.schedules())) {
            throw new NotExistingSchedulesException();
        }

        sendWelcomeMessage(discordApiId, token);

        Webhook discordWebhook = repository.save(
                Webhook.builder()
                        .discordId(discordApiId)
                        .discordToken(token)
                        .addedBy(jwt.getSubject())
                        .schedules(schedules)
                        .build()
        );

        return new WebhookResponse(
                discordWebhook.getId(),
                discordWebhook.getDiscordId(),
                discordWebhook.getDiscordToken(),
                discordWebhook.getSchedules()
        );
    }

    private boolean isNotValidDiscordWebhookUrl(String url) {
        if (!url.startsWith(DISCORD_WEBHOOK_URL_PREFIX)) {
            return true;
        }

        String[] parts = splitUrlIntoParts(url);

        if (parts.length != 2) {
            return true;
        }

        return parts[0].isEmpty() || parts[1].isEmpty();
    }

    public String[] splitUrlIntoParts(String url) {
        int beginIdx = DISCORD_WEBHOOK_URL_PREFIX.length();
        String afterPrefix = url.substring(beginIdx);

        return afterPrefix.split("/");
    }

    private String getDiscordApiIdFromUrl(String url) {
        return splitUrlIntoParts(url)[0];
    }

    private String getTokenFromUrl(String url) {
        return splitUrlIntoParts(url)[1];
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
        Webhook discordWebhook = findByIdOrElseThrow(id);

        String userId = securityService.getUserId(jwt);

        if (!discordWebhook.getAddedBy().equals(userId) && !securityService.isAdmin(jwt)) {
            throw new ForbiddenAccessException();
        }

        String discordWebhookUrl = request.url();

        if (isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new InvalidDiscordWebhookUrlException();
        }

        String discordApiId = getDiscordApiIdFromUrl(discordWebhookUrl);
        String currentDiscordApiId = discordWebhook.getDiscordId();

        if (isDiscordApiIdUnavailable(discordApiId, currentDiscordApiId)) {
            throw new UnavailableDiscordApiIdException();
        }

        String token = getTokenFromUrl(discordWebhookUrl);
        String currentToken = discordWebhook.getDiscordToken();

        if (isTokenUnavailable(token, currentToken)) {
            throw new UnavailableTokenException();
        }

        Set<UUID> currentSchedules = discordWebhook.getSchedules();
        Set<UUID> schedulesToCheck = request.schedules().stream()
                .filter(schedule -> !currentSchedules.contains(schedule))
                .collect(Collectors.toSet());

        if (doesAnyScheduleDoNotExist(schedulesToCheck)) {
            throw new NotExistingSchedulesException();
        }

        sendWelcomeMessage(discordApiId, token);

        discordWebhook.setDiscordId(discordApiId);
        discordWebhook.setDiscordToken(token);
        discordWebhook.setSchedules(request.schedules());

        discordWebhook = repository.save(discordWebhook);

        return mapper.map(discordWebhook);
    }

    private boolean isDiscordApiIdUnavailable(String discordApiId, String currentDiscordApiId) {
        return repository.findByDiscordApiId(discordApiId).isPresent()
                && !repository.findByDiscordApiId(discordApiId).get()
                .getId()
                .equals(currentDiscordApiId);
    }

    private boolean isTokenUnavailable(String token, String currentToken) {
        return repository.findByToken(token).isPresent()
                && !repository.findByToken(token).get()
                .getId()
                .equals(currentToken);
    }

    private boolean doesAnyScheduleDoNotExist(Set<UUID> schedules) {
        return !scheduleService.checkIfSchedulesExist(schedules);
    }

    private void sendWelcomeMessage(String discordApiId, String token) {
        discordApiWebhooksClient.sendMessage(
                discordApiId,
                token,
                new DiscordMessageRequest(WELCOME_MESSAGE)
        );
    }
}
