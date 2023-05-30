package com.example.discordnotificationservice.service;

import com.example.discordnotificationservice.client.ScheduleClient;
import com.example.discordnotificationservice.dto.ScheduleResponse;
import com.example.discordnotificationservice.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleClient client;

    @Test
    void GivenNonExistingSchedules_WhenCheckingIfSchedulesExist_ThenReturnsFalse() {
        // Given
        UUID scheduleId1 = UUID.randomUUID();
        UUID scheduleId2 = UUID.randomUUID();
        Set<UUID> scheduleIds = Set.of(scheduleId1, scheduleId2);

        Set<ScheduleResponse> retrievedSchedules = Set.of();

        when(client.findSelected(scheduleIds)).thenReturn(retrievedSchedules);

        // When
        boolean result = underTest.checkIfSchedulesExist(scheduleIds);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenSomeNonExistingSchedules_WhenCheckingIfSchedulesExist_ThenReturnsFalse() {
        // Given
        UUID scheduleId1 = UUID.randomUUID();
        UUID scheduleId2 = UUID.randomUUID();
        Set<UUID> scheduleIds = Set.of(scheduleId1, scheduleId2);

        ScheduleResponse scheduleResponse1 = new ScheduleResponse(scheduleId1);
        Set<ScheduleResponse> retrievedSchedules = Set.of(scheduleResponse1);

        when(client.findSelected(scheduleIds)).thenReturn(retrievedSchedules);

        // When
        boolean result = underTest.checkIfSchedulesExist(scheduleIds);

        // Then
        assertThat(result).isFalse();
    }
}