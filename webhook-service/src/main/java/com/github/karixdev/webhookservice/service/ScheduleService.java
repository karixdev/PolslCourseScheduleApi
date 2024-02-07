package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.webhookservice.client.ScheduleServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ScheduleServiceClient client;

	public boolean doSchedulesExist(Set<UUID> ids) {
		return client.find(ids)
				.stream()
				.map(ScheduleResponse::id)
				.collect(Collectors.toSet())
				.equals(ids);
	}

	public Optional<String> getScheduleName(UUID id) {
		return client.findById(id).map(ScheduleResponse::name);
	}

}
