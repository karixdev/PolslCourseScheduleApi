package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.webscraperservice.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.exception.EmptyTimeCellSetException;
import com.github.karixdev.webscraperservice.mapper.PlanPolslResponseMapper;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.props.PlanPolslClientProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanPolslServiceTest {

    @InjectMocks
    PlanPolslService underTest;

    @Mock
    PlanPolslClient client;

    @Mock
    PlanPolslResponseMapper mapper;

    @Test
    void GivenAttrSuchThatSiteReturnsExpectedResponse_WhenGetSchedule_ThenReturnsCorrectPlanPolslResponse() {
        // Given
        int planPolslId = 1000;
        int type = 0;
        int wd = 0;

        String clientResponse = """
                <div class="cd">07:00-08:00</div>
                <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                    This is course div
                </div>
                """;

        when(client.getSchedule(
                planPolslId,
                type,
                wd,
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H))
                .thenReturn(new ByteArrayResource(clientResponse.getBytes()));

        TimeCell timeCell = new TimeCell("07-00:08:00");
        CourseCell courseCell = new CourseCell(
                30,
                40,
                10,
                20,
                "This is course div"
        );

        when(mapper.map(any()))
                .thenReturn(new PlanPolslResponse(
                        Set.of(timeCell),
                        Set.of(courseCell)
                ));

        // When
        PlanPolslResponse result = underTest.getSchedule(
                planPolslId, type, wd
        );

        // Then
        assertThat(result)
                .isEqualTo(new PlanPolslResponse(
                        Set.of(timeCell),
                        Set.of(courseCell))
                );
    }

    @Test
    void GivenScheduleParamsSuchThatResultsWithEmptyCourseCellsSet_WhenGetSchedule_ThenThrowsEmptyCourseCellsSetException() {
        // Given
        int planPolslId = 1000;
        int type = 0;
        int wd = 0;

        when(client.getSchedule(
                planPolslId,
                type,
                wd,
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H))
                .thenReturn(new ByteArrayResource("<html></html>".getBytes()));

        when(mapper.map(any())).thenReturn(new PlanPolslResponse(Set.of(), Set.of()));

        // When & Then
        assertThatThrownBy(() -> underTest.getSchedule(planPolslId, type, wd))
                .isInstanceOf(EmptyCourseCellsSetException.class);
    }

    @Test
    void GivenScheduleParamSuchThatResultsWithEmptyTimeCellsSet_WhenGetSchedule_ThenThrowsEmptyTimeCellSetException() {
        // Given
        int planPolslId = 1000;
        int type = 0;
        int wd = 0;

        when(client.getSchedule(
                planPolslId,
                type,
                wd,
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H))
                .thenReturn(new ByteArrayResource("<html></html>".getBytes()));

        when(mapper.map(any()))
                .thenReturn(
                        new PlanPolslResponse(
                                Set.of(),
                                Set.of(CourseCell.builder().build())
                        )
                );

        // When & Then
        assertThatThrownBy(() -> underTest.getSchedule(planPolslId, type, wd))
                .isInstanceOf(EmptyTimeCellSetException.class);
    }

}
