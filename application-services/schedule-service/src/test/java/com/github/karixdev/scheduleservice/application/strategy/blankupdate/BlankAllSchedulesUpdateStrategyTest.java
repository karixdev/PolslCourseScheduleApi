package com.github.karixdev.scheduleservice.application.strategy.blankupdate;

import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageInfo;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlankAllSchedulesUpdateStrategyTest {

    @InjectMocks
    BlankAllSchedulesUpdateStrategy underTest;

    @Mock
    EventProducer<ScheduleEvent> eventProducer;

    @Mock
    ScheduleRepository repository;

    @Test
    void GivenEmptyList_WhenSupports_ThenReturnsTrue() {
        // Given
        List<UUID> ids = List.of();

        // When
        boolean result = underTest.supports(ids);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void GivenEmptyListOfIds_WhenBlankUpdate_ThenPerformsBlankUpdatesAndSendsEventUsingBatchesForAllSchedules() {
        // Given
        List<UUID> ids = List.of();

        Page<Schedule> firstPage = Page.<Schedule>builder()
                .content(createSchedules(10))
                .pageInfo(
                        PageInfo.builder()
                                .isLast(false)
                                .build()
                )
                .build();

        Page<Schedule> secondPage = Page.<Schedule>builder()
                .content(createSchedules(3))
                .pageInfo(
                        PageInfo.builder()
                                .isLast(true)
                                .build()
                )
                .build();

        PageRequest firstPr = new PageRequest(0, 10);
        PageRequest secondPr = new PageRequest(1, 10);

        when(repository.findAllPaginated(firstPr)).thenReturn(firstPage);
        when(repository.findAllPaginated(secondPr)).thenReturn(secondPage);

        // When
        underTest.blankUpdate(ids);

        // Then
        ArgumentCaptor<ScheduleEvent> captor = ArgumentCaptor.forClass(ScheduleEvent.class);
        verify(eventProducer, times(13)).produce(captor.capture());

        List<String> stringIds = Stream.concat(firstPage.content().stream(), secondPage.content().stream())
                .map(schedule -> schedule.getId().toString())
                .toList();
        List<String> eventsScheduleIds = captor.getAllValues().stream()
                .map(ScheduleEvent::scheduleId)
                .toList();

        assertThat(eventsScheduleIds).isEqualTo(stringIds);
    }

    private static List<Schedule> createSchedules(int num) {
        return IntStream.range(0, num).mapToObj(i -> {
            PlanPolslData planPolslData = PlanPolslData.builder()
                    .id(i + 1)
                    .type(i + 2)
                    .type(i + 3)
                    .build();

            return Schedule.builder()
                    .id(UUID.randomUUID())
                    .semester(i + 1)
                    .major("major-" + i)
                    .groupNumber(i + 2)
                    .planPolslData(planPolslData)
                    .build();
        }).toList();
    }

}