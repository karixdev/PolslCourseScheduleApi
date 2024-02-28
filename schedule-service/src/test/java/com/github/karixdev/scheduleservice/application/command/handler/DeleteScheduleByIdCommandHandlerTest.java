package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.DeleteScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionCallback;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteScheduleByIdCommandHandlerTest {

    @InjectMocks
    DeleteScheduleByIdCommandHandler underTest;

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
        DeleteScheduleByIdCommand command = DeleteScheduleByIdCommand.builder()
                .id(UUID.randomUUID())
                .build();

        when(repository.findById(command.id()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(ScheduleWithIdNotFoundException.class);
    }

    @Test
    void GivenValidCommand_WhenHandle_ThenDeletesScheduleAndProducesEvent() {
        // Given
        UUID id = UUID.randomUUID();

        DeleteScheduleByIdCommand command = DeleteScheduleByIdCommand.builder()
                .id(id)
                .build();

        Schedule schedule = Schedule.builder()
                .id(id)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule")
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback transactionCallback = transactionCallbackCaptor.getValue();
        transactionCallback.execute();

        verify(repository).delete(schedule);

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .type(EventType.DELETE)
                .scheduleId(id.toString())
                .entity(schedule)
                .build();

        verify(producer).produce(expectedEvent);
    }

}