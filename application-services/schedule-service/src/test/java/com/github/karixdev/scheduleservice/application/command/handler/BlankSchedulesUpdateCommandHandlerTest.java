package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.BlankSchedulesUpdateCommand;
import com.github.karixdev.scheduleservice.application.exception.NoBlankScheduleUpdateStrategyException;
import com.github.karixdev.scheduleservice.application.strategy.blankupdate.BlankSchedulesUpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlankSchedulesUpdateCommandHandlerTest {

    BlankSchedulesUpdateCommandHandler underTest;

    BlankSchedulesUpdateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = mock(BlankSchedulesUpdateStrategy.class);
        underTest = new BlankSchedulesUpdateCommandHandler(List.of(strategy));
    }

    @Test
    void GivenIdsThatNoStrategySupports_WhenHandle_ThenThrowsNoBlankScheduleUpdateStrategyException() {
        // Given
        List<UUID> ids = List.of(UUID.randomUUID());
        BlankSchedulesUpdateCommand command = new BlankSchedulesUpdateCommand(ids);

        when(strategy.supports(ids)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(NoBlankScheduleUpdateStrategyException.class);
    }

}