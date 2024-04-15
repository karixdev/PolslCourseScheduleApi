package com.github.karixdev.courseservice.application.strategy.event.handler.scheduleevent;

import com.github.karixdev.courseservice.application.dal.TransactionCallback;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.domain.entity.schedule.Schedule;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
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

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleEventDeleteTypeHandlerStrategyTest {

    @InjectMocks
    ScheduleEventDeleteTypeHandlerStrategy underTest;

    @Mock
    CourseRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @ParameterizedTest
    @MethodSource("notSupportedEventTypes")
    void GivenNotSupportedEventType_WhenSupports_ThenReturnsFalse(EventType eventType) {
        // When
        boolean result = underTest.supports(eventType);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenSupportedEventType_WhenSupports_ThenReturnsTrue() {
        // Given
        EventType eventType = EventType.DELETE;

        // When
        boolean result = underTest.supports(eventType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void GivenEvent_WhenHandle_ThenDeletesAllScheduleCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .entity(Schedule.builder().id(scheduleId).build())
                .build();

        // When
        underTest.handle(scheduleEvent);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback callback = transactionCallbackCaptor.getValue();
        callback.execute();

        verify(repository).deleteByScheduleId(scheduleId);
    }

    private static Stream<Arguments> notSupportedEventTypes() {
        return Stream.of(
                Arguments.of(EventType.CREATE),
                Arguments.of(EventType.UPDATE)
        );
    }

}