package com.example.discordnotificationservice.service;

import com.example.discordnotificationservice.client.ScheduleClient;
import com.example.discordnotificationservice.dto.ScheduleResponse;
import com.example.discordnotificationservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleClient client;

    public boolean checkIfSchedulesExist(Set<UUID> scheduleIds) {
        Set<ScheduleResponse> retrievedSchedules =
                client.findSelected(scheduleIds);

        return retrievedSchedules.stream()
                .map(ScheduleResponse::id)
                .collect(Collectors.toSet())
                .equals(scheduleIds);
    }

    public String getScheduleName(UUID scheduleId) {
        return client.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule with id %s not found".formatted(scheduleId)
                        )
                )
                .name();
    }
}
