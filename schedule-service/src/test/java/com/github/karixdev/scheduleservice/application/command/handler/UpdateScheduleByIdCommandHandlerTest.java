package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.command.UpdateScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionCallback;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.application.exception.UnavailablePlanPolslIdException;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.karixdev.scheduleservice.matcher.ScheduleWholeEntityArgumentMatcher.scheduleWholeEntityEq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateScheduleByIdCommandHandlerTest {

    @InjectMocks
    UpdateScheduleByIdCommandHandler underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Mock
    EventProducer<ScheduleEvent> producer;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Test
    void GivenCommandWithIdOfNotExistingSchedule_WhenHandle_ThenThrowsScheduleWithIdNotFoundException() {
        // Given
        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(UUID.randomUUID())
                .build();

        when(repository.findById(command.id()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(ScheduleWithIdNotFoundException.class);
    }

    @Test
    void GivenCommandWithUnavailablePlanPolslId_WhenHandle_ThenThrowsUnavailablePlanPolslIdException() {
        // Given
        UUID id = UUID.randomUUID();

        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(id)
                .semester(1)
                .major("new-major")
                .groupNumber(2)
                .planPolslId(31)
                .type(4)
                .weekDays(5)
                .build();

        Schedule schedule = Schedule.builder()
                .id(id)
                .semester(2)
                .groupNumber(3)
                .major("major")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(3)
                                .type(4)
                                .weekDays(5)
                                .build()
                )
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        when(repository.findByPlanPolslId(command.planPolslId()))
                .thenReturn(Optional.of(Schedule.builder().build()));

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(UnavailablePlanPolslIdException.class);
    }

    @Test
    void GivenValidCommand_WhenHandle_ThenSavesUpdated() {
        // Given
        UUID id = UUID.randomUUID();

        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(id)
                .semester(1)
                .major("new-major")
                .groupNumber(2)
                .planPolslId(3)
                .type(4)
                .weekDays(5)
                .build();

        Schedule schedule = Schedule.builder()
                .id(id)
                .semester(2)
                .groupNumber(3)
                .major("major")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(3)
                                .type(4)
                                .weekDays(5)
                                .build()
                )
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(id)
                .semester(command.semester())
                .groupNumber(command.groupNumber())
                .major(command.major())
                .planPolslData(
                        PlanPolslData.builder()
                                .id(command.planPolslId())
                                .type(command.type())
                                .weekDays(command.weekDays())
                                .build()
                )
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));
        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback transactionCallback = transactionCallbackCaptor.getValue();
        transactionCallback.execute();

        verify(repository).save(scheduleWholeEntityEq(updatedSchedule));
    }

    @Test
    void GivenCommandWithNotChangedPlanPolslDataProperties_WhenHandle_ThenSavesUpdatedScheduleAndDoesNotProduceEvent() {
        UUID id = UUID.randomUUID();

        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(id)
                .semester(1)
                .major("major")
                .groupNumber(2)
                .planPolslId(3)
                .type(4)
                .weekDays(5)
                .build();

        Schedule schedule = Schedule.builder()
                .id(id)
                .semester(2)
                .groupNumber(3)
                .major("major")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(3)
                                .type(4)
                                .weekDays(5)
                                .build()
                )
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(id)
                .semester(command.semester())
                .groupNumber(command.groupNumber())
                .major(command.major())
                .planPolslData(
                        PlanPolslData.builder()
                                .id(command.planPolslId())
                                .type(command.type())
                                .weekDays(command.weekDays())
                                .build()
                )
                .build();

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .scheduleId(schedule.getId().toString())
                .type(EventType.UPDATE)
                .entity(updatedSchedule)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback transactionCallback = transactionCallbackCaptor.getValue();
        transactionCallback.execute();

        verify(repository).save(scheduleWholeEntityEq(updatedSchedule));

        verify(producer, never()).produce(expectedEvent);
    }

    @ParameterizedTest
    @MethodSource("newPlanPolslDataProperties")
    void GivenCommandWithChangedPlanPolslDataProperty_WhenHandle_ThenSavesUpdatedScheduleAndProducesEvent(int planPolslId, int type, int weekDays) {
        UUID id = UUID.randomUUID();

        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(id)
                .semester(1)
                .major("major")
                .groupNumber(2)
                .planPolslId(planPolslId)
                .type(type)
                .weekDays(weekDays)
                .build();

        Schedule schedule = Schedule.builder()
                .id(id)
                .semester(2)
                .groupNumber(3)
                .major("major")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1)
                                .type(2)
                                .weekDays(3)
                                .build()
                )
                .build();

        Schedule updatedSchedule = Schedule.builder()
                .id(id)
                .semester(command.semester())
                .groupNumber(command.groupNumber())
                .major(command.major())
                .planPolslData(
                        PlanPolslData.builder()
                                .id(command.planPolslId())
                                .type(command.type())
                                .weekDays(command.weekDays())
                                .build()
                )
                .build();

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .scheduleId(schedule.getId().toString())
                .type(EventType.UPDATE)
                .entity(updatedSchedule)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback transactionCallback = transactionCallbackCaptor.getValue();
        transactionCallback.execute();

        verify(repository).save(scheduleWholeEntityEq(updatedSchedule));

        verify(producer).produce(expectedEvent);
    }

    private static Stream<Arguments> newPlanPolslDataProperties() {
        // planPolslId, type, weekDays
        return Stream.of(
                Arguments.of(4, 2, 3),
                Arguments.of(1, 4, 3),
                Arguments.of(1, 2, 4),
                Arguments.of(4, 5, 6)
        );
    }

}