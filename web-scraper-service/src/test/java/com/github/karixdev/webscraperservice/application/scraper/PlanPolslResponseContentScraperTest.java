package com.github.karixdev.webscraperservice.application.scraper;

import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import com.github.karixdev.webscraperservice.domain.RawAnchor;
import com.github.karixdev.webscraperservice.domain.RawCourse;
import com.github.karixdev.webscraperservice.domain.RawSchedule;
import com.github.karixdev.webscraperservice.domain.RawTimeInterval;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlanPolslResponseContentScraperTest {

    PlanPolslResponseContentScraper underTest;

    @BeforeEach
    void setUp() {
        underTest = new PlanPolslResponseContentScraper(
                new ElementAttributesScraper(),
                new CSSPropertiesScraper()
        );
    }

    @Test
    void GivenPlanPolslResponse_WhenScrapSchedule_ThenReturnCorrectRawSchedule() {
        // Given
        String html = """
                <div class="cd">ab:cd-ef:gh</div>
                <div class="cd">a1:c2-4f:g1</div>
                <div class="cd"></div>
                <div class="cd">08:00-09:00</div>
                
                <div class="coursediv"
                     cw="10"
                     ch="20"
                     style="left: 30px; top: 40px; background-color: #fff"
                >
                course
                <a href="link-address">link-text</a>
                </div>
                """;

        PlanPolslResponse planPolslResponse = PlanPolslResponse.builder()
                .content(Jsoup.parse(html))
                .build();

        // When
        RawSchedule result = underTest.scrapSchedule(planPolslResponse);

        // Then
        RawTimeInterval timeInterval = RawTimeInterval.builder()
                .start("08:00")
                .end("09:00")
                .build();

        RawAnchor anchor = RawAnchor.builder()
                .text("link-text")
                .address("link-address")
                .build();

        RawCourse course = RawCourse.builder()
                .text("course")
                .height(20)
                .width(10)
                .left(30)
                .top(40)
                .anchors(Set.of(anchor))
                .build();

        RawSchedule expected = RawSchedule.builder()
                .courses(Set.of(course))
                .timeIntervals(Set.of(timeInterval))
                .build();

        assertThat(result).isEqualTo(expected);
    }

}