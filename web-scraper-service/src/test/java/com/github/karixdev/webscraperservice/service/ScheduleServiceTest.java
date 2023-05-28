package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.course.CourseMapper;
import com.github.karixdev.webscraperservice.course.TimeService;
import com.github.karixdev.webscraperservice.service.PlanPolslService;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.message.ScheduleUpdateRequestMessage;
import com.github.karixdev.webscraperservice.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    PlanPolslService planPolslService;

    @Test
    void GivenScheduleUpdateRequestMessageSuchThatPlanPolslClientReturnsResponseWithEmptyCourseCellsSet_WhenUpdateSchedule_ThenThrowsEmptyCourseCellsSetExceptionWithProperMessage() {
        // Given
        var message = new ScheduleUpdateRequestMessage(
                UUID.randomUUID(),
                0,
                1337,
                4
        );

        when(planPolslService.getSchedule(anyInt(), anyInt(), anyInt()))
                .thenReturn(new PlanPolslResponse(Set.of(), Set.of()));

        // When & Then
        assertThatThrownBy(() -> underTest.updateSchedule(message))
                .isInstanceOf(EmptyCourseCellsSetException.class)
                .hasMessage("Course cells set is empty");
    }
}
