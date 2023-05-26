package com.example.discordnotificationservice.schedule;

import com.example.discordnotificationservice.schedule.dto.ScheduleResponse;
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
}
