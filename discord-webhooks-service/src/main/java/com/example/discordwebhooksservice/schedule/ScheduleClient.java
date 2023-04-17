package com.example.discordwebhooksservice.schedule;

import com.example.discordwebhooksservice.schedule.dto.ScheduleResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Set;
import java.util.UUID;

@HttpExchange("/api/schedules")
public interface ScheduleClient {
    @GetExchange
    Set<ScheduleResponse> findSelected(
            @RequestParam(name = "ids") Set<UUID> ids
    );
}
