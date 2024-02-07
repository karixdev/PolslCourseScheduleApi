package com.github.karixdev.webhookservice.service;

import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.webhookservice.client.ScheduleServiceClient;
import org.apache.zookeeper.Op;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

	@InjectMocks
	ScheduleService underTest;

	@Mock
	ScheduleServiceClient client;

	@Test
	void GivenNonExistingSchedulesIds_WhenDoSchedulesExist_ThenReturnsFalse() {
		// Given
		UUID scheduleId1 = UUID.randomUUID();
		UUID scheduleId2 = UUID.randomUUID();
		Set<UUID> scheduleIds = Set.of(scheduleId1, scheduleId2);

		Set<ScheduleResponse> retrievedSchedules = Set.of(ScheduleResponse.builder().id(scheduleId1).build());

		when(client.find(scheduleIds)).thenReturn(retrievedSchedules);

		// When
		boolean result = underTest.doSchedulesExist(scheduleIds);

		// Then
		assertThat(result).isFalse();
	}

	@Test
	void GivenExistingSchedulesIds_WhenDoSchedulesExist_ThenReturnsTrue() {
		// Given
		UUID scheduleId1 = UUID.randomUUID();
		UUID scheduleId2 = UUID.randomUUID();
		Set<UUID> scheduleIds = Set.of(scheduleId1, scheduleId2);

		Set<ScheduleResponse> retrievedSchedules = Set.of(
				ScheduleResponse.builder().id(scheduleId1).build(),
				ScheduleResponse.builder().id(scheduleId2).build()
		);

		when(client.find(scheduleIds)).thenReturn(retrievedSchedules);

		// When
		boolean result = underTest.doSchedulesExist(scheduleIds);

		// Then
		assertThat(result).isTrue();
	}

	@Test
	void GivenScheduleIdThatScheduleServiceClientReturnsNotEmptyOptional_WhenGetScheduleName_ThenReturnsOptionalWithScheduleName() {
		// Given
		UUID id = UUID.randomUUID();

		ScheduleResponse scheduleResponse = ScheduleResponse.builder()
				.id(id)
				.name("name")
				.build();

		when(client.findById(id)).thenReturn(Optional.of(scheduleResponse));

		// When
		Optional<String> result = underTest.getScheduleName(id);

		// Then
		assertThat(result)
				.isPresent()
				.contains(scheduleResponse.name());
	}

	@Test
	void GivenScheduleIdThatScheduleServiceClientReturnsEmptyOptional_WhenGetScheduleName_ThenReturnsEmptyOptional() {
		// Given
		UUID id = UUID.randomUUID();

		when(client.findById(id)).thenReturn(Optional.empty());

		// When
		Optional<String> result = underTest.getScheduleName(id);

		// Then
		assertThat(result)
				.isEmpty();
	}

}