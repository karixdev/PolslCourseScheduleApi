package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebHookInvalidUrlException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.DiscordWebHookUrlNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.discord.exception.EmptySchedulesIdsSetException;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebHookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebHookResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.PermissionDeniedException;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscordWebHookService {
    private final DiscordWebHookRepository repository;
    private final ScheduleService scheduleService;
    private final DiscordApiService discordApiService;
    private final DiscordProperties properties;

    @Transactional
    public DiscordWebHookResponse create(DiscordWebHookRequest payload, UserPrincipal userPrincipal) {
        String url = payload.getUrl();

        if (!url.startsWith(properties.getWebHookBaseUrl())) {
            throw new DiscordWebHookInvalidUrlException();
        }

        if (repository.findByUrl(url).isPresent()) {
            throw new DiscordWebHookUrlNotAvailableException();
        }

        if (payload.getSchedulesIds().isEmpty()) {
            throw new EmptySchedulesIdsSetException();
        }

        Set<Schedule> schedules = payload.getSchedulesIds().stream()
                .map(scheduleService::getById)
                .collect(Collectors.toSet());

        DiscordWebHook discordWebHook = repository.save(
                DiscordWebHook.builder()
                        .url(url)
                        .schedules(schedules)
                        .addedBy(userPrincipal.getUser())
                        .build());

        discordApiService.sendWelcomeMessage(
                discordWebHook.getUrl());

        return new DiscordWebHookResponse(discordWebHook);
    }

    public SuccessResponse delete(Long id, UserPrincipal userPrincipal) {
        DiscordWebHook discordWebHook = repository.findById(id)
                .orElseThrow(() -> {
                   throw new ResourceNotFoundException(
                           "Discord webhook with provided id not found");
                });

        User user = userPrincipal.getUser();

        if (user.getUserRole() != UserRole.ROLE_ADMIN &&
            !discordWebHook.getAddedBy().equals(user)) {
            throw new PermissionDeniedException(
                    "You are not the owner of the Discord webhook");
        }

        repository.delete(discordWebHook);

        return new SuccessResponse();
    }
}
