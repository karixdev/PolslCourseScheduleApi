package com.github.karixdev.courseservice.application.event.handler;

import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent.IgnoredScheduleEventTypeHandlerStrategy;
import com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent.ScheduleEventConcreteTypeHandlerStrategy;
import com.github.karixdev.courseservice.domain.entity.schedule.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ScheduleEventHandlerTest {

    ScheduleEventHandler underTest;

    List<ScheduleEventConcreteTypeHandlerStrategy> concreteTypeHandlerStrategies;
    ScheduleEventConcreteTypeHandlerStrategy mockStrategy1;
    ScheduleEventConcreteTypeHandlerStrategy mockStrategy2;

    IgnoredScheduleEventTypeHandlerStrategy ignoredScheduleEventTypeHandlerStrategy;

    @BeforeEach
    void setUp() {
        ignoredScheduleEventTypeHandlerStrategy = mock(IgnoredScheduleEventTypeHandlerStrategy.class);

        mockStrategy1 = mock(ScheduleEventConcreteTypeHandlerStrategy.class);
        mockStrategy2 = mock(ScheduleEventConcreteTypeHandlerStrategy.class);
        concreteTypeHandlerStrategies = List.of(mockStrategy1, mockStrategy2);

        underTest = new ScheduleEventHandler(concreteTypeHandlerStrategies, ignoredScheduleEventTypeHandlerStrategy);
    }

    @Test
    void GivenEventWithIgnoreType_WhenHandle_ThenExecutesIgnoredScheduleEventTypeHandlerStrategy() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .type(EventType.CREATE)
                .entity(Schedule.builder().id(scheduleId).build())
                .build();

        when(mockStrategy1.supports(scheduleEvent.type()))
                .thenReturn(false);

        when(mockStrategy2.supports(scheduleEvent.type()))
                .thenReturn(false);

        // When
        underTest.handle(scheduleEvent);

        // Then
        verify(ignoredScheduleEventTypeHandlerStrategy).handle(scheduleEvent);
        verify(mockStrategy1, never()).handle(scheduleEvent);
        verify(mockStrategy2, never()).handle(scheduleEvent);
    }

    @Test
    void GivenEventWithSupportedType_WhenHandle_ThenExecutesCorrectStrategy() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .type(EventType.CREATE)
                .entity(Schedule.builder().id(scheduleId).build())
                .build();

        when(mockStrategy1.supports(scheduleEvent.type()))
                .thenReturn(true);

        when(mockStrategy2.supports(scheduleEvent.type()))
                .thenReturn(false);

        // When
        underTest.handle(scheduleEvent);

        // Then
        verify(mockStrategy1).handle(scheduleEvent);
        verify(mockStrategy2, never()).handle(scheduleEvent);
        verify(ignoredScheduleEventTypeHandlerStrategy, never()).handle(scheduleEvent);
    }

}