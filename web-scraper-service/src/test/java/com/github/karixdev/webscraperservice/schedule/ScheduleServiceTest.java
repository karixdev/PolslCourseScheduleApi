package com.github.karixdev.webscraperservice.schedule;

import com.github.karixdev.webscraperservice.course.CourseMapper;
import com.github.karixdev.webscraperservice.course.TimeService;
import com.github.karixdev.webscraperservice.planpolsl.PlanPolslClient;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateRequestMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    PlanPolslClient planPolslClient;

    @Test
    void GivenScheduleUpdateRequestMessageSuchThatPlanPolslClientReturnsResponseWithEmptyCourseCellsSet_WhenUpdateSchedule_ThenThrowsEmptyCourseCellsSetExceptionWithProperMessage() {
        // Given
        var message = new ScheduleUpdateRequestMessage(
                UUID.randomUUID(),
                0,
                1337,
                4
        );

        when(planPolslClient.getSchedule(anyInt(), anyInt(), anyInt()))
                .thenReturn(new PlanPolslResponse(Set.of(), Set.of()));

        // When & Then
        assertThatThrownBy(() -> underTest.updateSchedule(message))
                .isInstanceOf(EmptyCourseCellsSetException.class)
                .hasMessage("Course cells set is empty");
    }
}
