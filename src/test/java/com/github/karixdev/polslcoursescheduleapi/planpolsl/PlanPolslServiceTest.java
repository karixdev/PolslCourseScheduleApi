package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import com.github.karixdev.polslcoursescheduleapi.planpolsl.exception.PlanPolslEmptyResponseException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.TimeCell;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WireMockTest(httpPort = 8888)
public class PlanPolslServiceTest {
    PlanPolslService underTest;

    PlanPolslProperties properties;

    PlanPolslResponseMapper responseMapper;

    Schedule schedule;

    User user;

    @BeforeEach
    void setUp() {
        properties = mock(PlanPolslProperties.class);
        responseMapper = mock(PlanPolslResponseMapper.class);

        WebClient webClient = WebClient.builder()
                .build();

        underTest = new PlanPolslService(
                webClient,
                properties,
                responseMapper);

        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(101)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();

        when(properties.getBaseUrl())
                .thenReturn("http://localhost:8888/plan");

        when(properties.getWinW())
                .thenReturn(1000);

        when(properties.getWinH())
                .thenReturn(1000);
    }

    @Test
    void GivenScheduleThatPlanPolslReturnsEmptyResponse_WhenGetCourses_ThenThrowsPlanPolslEmptyResponseExceptionWithProperMessage() {
        stubFor(get(urlPathEqualTo("/plan"))
                .withQueryParam("id", equalTo("101"))
                .withQueryParam("type", equalTo("0"))
                .withQueryParam("winH", equalTo("1000"))
                .withQueryParam("winW", equalTo("1000"))
                .willReturn(ok()));

        // When & Then
        assertThatThrownBy(() -> underTest.getPlanPolslResponse(schedule))
                .isInstanceOf(PlanPolslEmptyResponseException.class)
                .hasMessage("plan.polsl.pl returned empty response");
    }

    @Test
    void GivenSchedule_WhenGetCourses_ThenReturnsCorrectPlanPolslResponse() {
        stubFor(get(urlPathEqualTo("/plan"))
                .withQueryParam("id", equalTo("101"))
                .withQueryParam("type", equalTo("0"))
                .withQueryParam("winH", equalTo("1000"))
                .withQueryParam("winW", equalTo("1000"))
                .willReturn(ok().withBody("""
                        <div class="cd">07:00-08:00</div>
                        <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                            This is course div
                        </div>
                        """)));

        CourseCell courseCell = new CourseCell(
                30, 40, 10, 20, "This is course div");

        TimeCell timeCell = new TimeCell("07-00:08:00");

        when(responseMapper.getCourseCells(any()))
                .thenReturn(List.of(courseCell));

        when(responseMapper.getTimeCells(any()))
                .thenReturn(List.of(timeCell));

        // When
        PlanPolslResponse result = underTest.getPlanPolslResponse(schedule);

        // Then
        assertThat(result.getCourseCells()).hasSize(1);
        assertThat(result.getCourseCells().get(0)).isEqualTo(courseCell);

        assertThat(result.getTimeCells()).hasSize(1);
        assertThat(result.getTimeCells().get(0)).isEqualTo(timeCell);
    }
}
