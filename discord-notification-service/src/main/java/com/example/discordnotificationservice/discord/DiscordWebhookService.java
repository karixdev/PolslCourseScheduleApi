package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.document.DiscordWebhook;
import com.example.discordnotificationservice.discord.dto.DiscordMessageRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import com.example.discordnotificationservice.discord.exception.InvalidDiscordWebhookUrlException;
import com.example.discordnotificationservice.discord.exception.NotExistingSchedulesException;
import com.example.discordnotificationservice.discord.exception.UnavailableDiscordApiIdException;
import com.example.discordnotificationservice.discord.exception.UnavailableTokenException;
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
public class DiscordWebhookService {
    private final DiscordApiWebhooksClient discordApiWebhooksClient;
    private final ScheduleService scheduleService;
    private final DiscordWebhookRepository repository;
    private final SecurityService securityService;
    private final DiscordWebhookDTOMapper mapper;

    private static final String DISCORD_WEBHOOK_URL_PREFIX = "https://discord.com/api/webhooks/";
    private static final String WELCOME_MESSAGE = "Hello form PolslCourseApi!";

    private static final int PAGE_SIZE = 10;

    @Transactional
    public DiscordWebhookResponse create(DiscordWebhookRequest request, Jwt jwt) {
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

        DiscordWebhook discordWebhook = repository.save(
                DiscordWebhook.builder()
                        .discordApiId(discordApiId)
                        .token(token)
                        .addedBy(jwt.getSubject())
                        .schedules(schedules)
                        .build()
        );

        return new DiscordWebhookResponse(
                discordWebhook.getId(),
                discordWebhook.getDiscordApiId(),
                discordWebhook.getToken(),
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

    public Page<DiscordWebhookResponse> findAll(Jwt jwt, Integer page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

        String userId = securityService.getUserId(jwt);

        Page<DiscordWebhook> discordWebhooks = securityService.isAdmin(jwt) ?
                repository.findAll(pageRequest) :
                repository.findByAddedBy(userId, pageRequest);

        return discordWebhooks.map(mapper::map);
    }

    private DiscordWebhook findByIdOrElseThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DiscordWebhook with id %s not found".formatted(id)));
    }

    @Transactional
    public void delete(String id, Jwt jwt) {
        DiscordWebhook discordWebhook = findByIdOrElseThrow(id);

        String userId = securityService.getUserId(jwt);

        if (!discordWebhook.getAddedBy().equals(userId) && !securityService.isAdmin(jwt)) {
            throw new ForbiddenAccessException();
        }

        repository.delete(discordWebhook);
    }

    @Transactional
    public DiscordWebhookResponse update(DiscordWebhookRequest request, Jwt jwt, String id) {
        DiscordWebhook discordWebhook = findByIdOrElseThrow(id);

        String userId = securityService.getUserId(jwt);

        if (!discordWebhook.getAddedBy().equals(userId) && !securityService.isAdmin(jwt)) {
            throw new ForbiddenAccessException();
        }

        String discordWebhookUrl = request.url();

        if (isNotValidDiscordWebhookUrl(discordWebhookUrl)) {
            throw new InvalidDiscordWebhookUrlException();
        }

        String discordApiId = getDiscordApiIdFromUrl(discordWebhookUrl);
        String currentDiscordApiId = discordWebhook.getDiscordApiId();

        if (isDiscordApiIdUnavailable(discordApiId, currentDiscordApiId)) {
            throw new UnavailableDiscordApiIdException();
        }

        String token = getTokenFromUrl(discordWebhookUrl);
        String currentToken = discordWebhook.getToken();

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

        discordWebhook.setDiscordApiId(discordApiId);
        discordWebhook.setToken(token);
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
