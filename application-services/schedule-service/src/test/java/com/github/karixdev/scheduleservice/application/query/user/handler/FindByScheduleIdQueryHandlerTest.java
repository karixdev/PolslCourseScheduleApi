package com.github.karixdev.scheduleservice.application.query.user.handler;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.query.user.FindScheduleByIdQuery;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindByScheduleIdQueryHandlerTest {

    @InjectMocks
    FindScheduleByIdQueryHandler underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    ModelMapper<Schedule, PublicScheduleDTO> mapper;

    @Test
    void GivenNotExistingScheduleId_WhenHandle_ThenThrowsScheduleWithIdNotFoundException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        FindScheduleByIdQuery query = new FindScheduleByIdQuery(scheduleId);

        when(repository.findById(scheduleId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.handle(query))
                .isInstanceOf(ScheduleWithIdNotFoundException.class);

        verify(mapper, never()).map(any());
    }

}