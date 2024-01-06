package com.github.karixdev.webhookservice.client;

import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@HttpExchange("/api/schedules")
public interface ScheduleServiceClient {

	@GetExchange
	Set<ScheduleResponse> find(
			@RequestParam(name = "ids") Set<UUID> ids
	);

	@GetExchange("/{id}")
	Optional<ScheduleResponse> findById(
			@PathVariable(name = "id") UUID id
	);

}
