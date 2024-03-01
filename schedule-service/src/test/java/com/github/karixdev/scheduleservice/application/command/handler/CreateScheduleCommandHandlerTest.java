package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionCallback;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.github.karixdev.scheduleservice.matcher.ScheduleNonIdArgumentMatcher.scheduleNonIdEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateScheduleCommandHandlerTest {

    @InjectMocks
    CreateScheduleCommandHandler underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Mock
    EventProducer<ScheduleEvent> producer;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Captor
    ArgumentCaptor<Schedule> scheduleCaptor;

    @Test
    void GivenValidCommand_WhenHandle_ThenSavesScheduleAndProducesEvent() {
        // Given
        CreateScheduleCommand command = CreateScheduleCommand.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .weekDays(4)
                .major("schedule")
                .build();

        Schedule expectedSchedule = Schedule.builder()
                .semester(2)
                .groupNumber(3)
                .major("schedule")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1)
                                .type(0)
                                .weekDays(4)
                                .build()
                )
                .build();

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback transactionCallback = transactionCallbackCaptor.getValue();
        transactionCallback.execute();

        verify(repository).save(scheduleNonIdEq(expectedSchedule));

        verify(repository).save(scheduleCaptor.capture());
        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .type(EventType.CREATE)
                .scheduleId(scheduleCaptor.getValue().getId().toString())
                .entity(scheduleCaptor.getValue())
                .build();

        verify(producer).produce(expectedEvent);
    }

}