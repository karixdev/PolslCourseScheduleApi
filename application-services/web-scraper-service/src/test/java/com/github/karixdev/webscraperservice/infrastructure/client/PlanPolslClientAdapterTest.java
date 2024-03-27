package com.github.karixdev.webscraperservice.infrastructure.client;

import com.github.karixdev.webscraperservice.application.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import com.github.karixdev.webscraperservice.application.props.PlanPolslClientProperties;
import com.github.karixdev.webscraperservice.infrastructure.client.exception.PlanPolslUnavailableException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = {
        PlanPolslClientConfig.class,
        ObservationAutoConfiguration.class
})
@WireMockTest(httpPort = 9999)
class PlanPolslClientAdapterTest {

    @Autowired
    PlanPolslClient underTest;

    @DynamicPropertySource
    static void overridePlanPolslUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "plan-polsl-url",
                () -> "http://localhost:9999");
    }

    @Test
    void GivenAttrsThatSiteRespondsWithErrorStatus_WhenGetSchedule_ThenThrowsPlanPolslUnavailableException() {
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
        assertThatThrownBy(() ->
                underTest.getSchedule(
                        planPolslId,
                        type,
                        wd,
                        PlanPolslClientProperties.WIN_W,
                        PlanPolslClientProperties.WIN_H
                ))
                .isInstanceOf(PlanPolslUnavailableException.class)
                .hasMessage("plan.polsl.pl responded with error status code 404");
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

        // When
        PlanPolslResponse result = underTest.getSchedule(
                planPolslId,
                type,
                wd,
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H
        );

        // Then
        Document expectedResponseContent = Jsoup.parse("""
                <div class="cd">07:00-08:00</div>
                <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                    This is course div
                </div>
                """);

        PlanPolslResponse expectedResponse = PlanPolslResponse.builder()
                .content(expectedResponseContent)
                .build();

        assertThat(result.toString()).hasToString(expectedResponse.toString());
    }

}