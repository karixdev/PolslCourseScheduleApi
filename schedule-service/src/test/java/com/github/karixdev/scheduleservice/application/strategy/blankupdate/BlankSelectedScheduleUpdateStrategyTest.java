package com.github.karixdev.scheduleservice.application.strategy.blankupdate;

import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlankSelectedScheduleUpdateStrategyTest {

    @InjectMocks
    BlankSelectedScheduleUpdateStrategy underTest;

    @Mock
    EventProducer<ScheduleEvent> eventProducer;

    @Mock
    ScheduleRepository repository;

    @Test
    void GivenEmptyList_WhenSupports_ThenReturnsFalse() {
        // Given
        List<UUID> ids = List.of();

        // When
        boolean result = underTest.supports(ids);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenNonEmptyList_WhenSupports_ThenReturnsTrue() {
        // Given
        List<UUID> ids = List.of(UUID.randomUUID());

        // When
        boolean result = underTest.supports(ids);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void GivenListOfIds_WhenBlankUpdate_ThenProducesEvent() {
        // Given
        UUID id = UUID.randomUUID();
        List<UUID> ids = List.of(id);

        PlanPolslData planPolslData = PlanPolslData.builder()
                .id(1)
                .type(2)
                .type(3)
                .build();

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(4)
                .major("major")
                .groupNumber(5)
                .planPolslData(planPolslData)
                .build();

        when(repository.findByIds(ids))
                .thenReturn(List.of(schedule));

        // When
        underTest.blankUpdate(ids);

        // Then
        ArgumentCaptor<ScheduleEvent> captor = ArgumentCaptor.forClass(ScheduleEvent.class);
        verify(eventProducer).produce(captor.capture());

        ScheduleEvent event = captor.getValue();
        assertThat(event.scheduleId()).isEqualTo(schedule.getId().toString());
    }

}