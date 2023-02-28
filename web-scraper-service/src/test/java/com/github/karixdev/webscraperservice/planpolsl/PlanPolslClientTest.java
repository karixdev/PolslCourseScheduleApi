package com.github.karixdev.webscraperservice.planpolsl;

import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import com.github.karixdev.webscraperservice.planpolsl.exception.PlanPolslUnavailableException;
import com.github.karixdev.webscraperservice.planpolsl.PlanPolslAdapter;
import com.github.karixdev.webscraperservice.planpolsl.PlanPolslClient;
import com.github.karixdev.webscraperservice.planpolsl.properties.PlanPolslClientProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WireMockTest(httpPort = 8888)
public class PlanPolslClientTest {
    PlanPolslClient underTest;

    PlanPolslAdapter planPolslAdapter;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8888/")
                .build();

        planPolslAdapter = mock(PlanPolslAdapter.class);

        underTest = new PlanPolslClient(
                webClient,
                planPolslAdapter
        );
    }

    @Test
    void GivenAttrsThatSiteRespondsWith4xx_WhenGetSchedule_ThenThrowsPlanPolslUnavailableException() {
        // Given
        int planPolslId = 1337;
        int type = 0;
        int wd = 4;

        stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id",   equalTo(String.valueOf(planPolslId)))
                .withQueryParam("type", equalTo(String.valueOf(type)))
                .withQueryParam("wd",   equalTo(String.valueOf(wd)))
                .withQueryParam("winH", equalTo(String.valueOf(PlanPolslClientProperties.WIN_W)))
                .withQueryParam("winW", equalTo(String.valueOf(PlanPolslClientProperties.WIN_H)))

                .willReturn(notFound())
        );

        // When & Then
        assertThatThrownBy(() -> underTest.getSchedule(planPolslId, type, wd))
                .isInstanceOf(PlanPolslUnavailableException.class)
                .hasMessage("plan.polsl.pl responded with status: 404 NOT_FOUND")
                .hasFieldOrPropertyWithValue("planPolslId", planPolslId)
                .hasFieldOrPropertyWithValue("type", type)
                .hasFieldOrPropertyWithValue("wd", wd);
    }

    @Test
    void GivenAttrsThatSiteReturnsEmptyResponse_WhenGetSchedule_ThenThrowsPlanPolslUnavailableException() {
        // Given
        int planPolslId = 2000;
        int type = 1;
        int wd = 3;

        stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id",   equalTo(String.valueOf(planPolslId)))
                .withQueryParam("type", equalTo(String.valueOf(type)))
                .withQueryParam("wd",   equalTo(String.valueOf(wd)))
                .withQueryParam("winH", equalTo(String.valueOf(PlanPolslClientProperties.WIN_W)))
                .withQueryParam("winW", equalTo(String.valueOf(PlanPolslClientProperties.WIN_H)))

                .willReturn(ok())
        );

        // When & Then
        assertThatThrownBy(() -> underTest.getSchedule(planPolslId, type, wd))
                .isInstanceOf(PlanPolslUnavailableException.class)
                .hasMessage("plan.polsl.pl responded with empty body")
                .hasFieldOrPropertyWithValue("planPolslId", planPolslId)
                .hasFieldOrPropertyWithValue("type", type)
                .hasFieldOrPropertyWithValue("wd", wd);
    }

    @Test
    void GivenAttrSuchThatSiteReturnsExpectedResponse_WhenGetSchedule_ThenReturnsCorrectPlanPolslResponse() {
        // Given
        int planPolslId = 1000;
        int type = 0;
        int wd = 0;

        stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id",   equalTo(String.valueOf(planPolslId)))
                .withQueryParam("type", equalTo(String.valueOf(type)))
                .withQueryParam("wd",   equalTo(String.valueOf(wd)))
                .withQueryParam("winH", equalTo(String.valueOf(PlanPolslClientProperties.WIN_W)))
                .withQueryParam("winW", equalTo(String.valueOf(PlanPolslClientProperties.WIN_H)))

                .willReturn(ok().withBody("""
                        <div class="cd">07:00-08:00</div>
                        <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                            This is course div
                        </div>
                        """)
        ));

        TimeCell timeCell = new TimeCell("07-00:08:00");
        CourseCell courseCell = new CourseCell(
                30,
                40,
                10,
                20,
                "This is course div"
        );

        when(planPolslAdapter.getTimeCells(any()))
                .thenReturn(Set.of(timeCell));

        when(planPolslAdapter.getCourseCells(any()))
                .thenReturn(Set.of(courseCell));

        // When
        PlanPolslResponse result = underTest.getSchedule(
                planPolslId, type, wd);

        // Then
        assertThat(result)
                .isEqualTo(new PlanPolslResponse(
                        Set.of(timeCell),
                        Set.of(courseCell))
                );
    }
}
