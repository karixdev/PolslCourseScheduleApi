package com.github.karixdev.webscraperservice.mapper;

import com.github.karixdev.webscraperservice.mapper.PlanPolslResponseMapper;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.Link;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.model.TimeCell;
import com.github.karixdev.webscraperservice.service.HtmlElementService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanPolslResponseMapperTest {
    @InjectMocks
    PlanPolslResponseMapper underTest;

    @Mock
    HtmlElementService htmlElementService;

    @Test
    void GivenDocumentWithTimeCellContainingTooShortText_WhenMap_ThenReturnsPlanPolslResponseWithEmptyTimeCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">abcd</div>
                """);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.timeCells()).isEmpty();
    }

    @Test
    void GivenDocumentWithTimeCellContainingTextNotMatchingRegex_WhenMap_ThenReturnsPlanPolslResponseWithEmptyTimeCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">ab:cd-ef:gh</div>
                """);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.timeCells()).isEmpty();
    }

    @Test
    void GivenDocumentWithValidTimeCell_WhenMap_ThenReturnsThenReturnsPlanPolslResponseWithProperTimeCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">07:00-08:00</div>
                """);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.timeCells()).isEqualTo(Set.of(
                new TimeCell("07:00-08:00")
        ));
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidStyles_WhenMap_ThenReturnsPlanPolslResponseWithEmptyCourseCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" cw="10" ch="10">
                    This is course div
                </div>
                """);

        when(htmlElementService.getSizeAttr(any(), any()))
                .thenReturn(10);

        when(htmlElementService.getStylesAttr(any()))
                .thenReturn(Map.of());

        when(htmlElementService.getCssSizeProperty(any(), any()))
                .thenReturn(0);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.courseCells()).isEmpty();
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidCwAndChAttrs_WhenMap__ThenReturnsPlanPolslResponseWithEmptyCourseCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="left: 10px; top: 10px;">
                    This is course div
                </div>
                """);

        when(htmlElementService.getSizeAttr(any(), any()))
                .thenReturn(0);

        when(htmlElementService.getStylesAttr(any()))
                .thenReturn(Map.of(
                        "left", "10",
                        "top", "10"
                ));

        when(htmlElementService.getSizeAttr(any(), any()))
                .thenReturn(10);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.courseCells()).isEmpty();
    }

    @Test
    void GivenDocumentWithCourseCellWithEmptyText_WhenMap__ThenReturnsPlanPolslResponseWithEmptyCourseCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                    
                </div>
                """);

        when(htmlElementService.getSizeAttr(any(), eq("ch")))
                .thenReturn(10);

        when(htmlElementService.getSizeAttr(any(), eq("cw")))
                .thenReturn(20);

        when(htmlElementService.getStylesAttr(any()))
                .thenReturn(Map.of(
                        "left", "40",
                        "top", "30"
                ));

        when(htmlElementService.getCssSizeProperty(any(), eq("top")))
                .thenReturn(30);


        when(htmlElementService.getCssSizeProperty(any(), eq("left")))
                .thenReturn(40);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        assertThat(result.courseCells()).isEmpty();
    }

    @Test
    void GivenDocumentWithValidCourseCell_WhenMap__ThenReturnsPlanPolslResponseWithProperCourseCellsSet() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                    This is course div
                    <a href="https://site.com">Site</a>
                    <a href="https://other-site.com">Other site</a>
                </div>
                """);

        when(htmlElementService.getSizeAttr(any(), eq("ch")))
                .thenReturn(10);

        when(htmlElementService.getSizeAttr(any(), eq("cw")))
                .thenReturn(20);

        when(htmlElementService.getStylesAttr(any()))
                .thenReturn(Map.of(
                        "left", "40",
                        "top", "30"
                ));

        when(htmlElementService.getCssSizeProperty(any(), eq("top")))
                .thenReturn(30);


        when(htmlElementService.getCssSizeProperty(any(), eq("left")))
                .thenReturn(40);

        // When
        PlanPolslResponse result = underTest.map(document);

        // Then
        Link expectedLink1 = new Link(
                "Site",
                "https://site.com"
        );
        Link expectedLink2 = new Link(
                "Other site",
                "https://other-site.com"
        );

        assertThat(result.courseCells()).isEqualTo(Set.of(
                new CourseCell(
                        30,
                        40,
                        10,
                        20,
                        "This is course div",
                        Set.of(expectedLink1, expectedLink2)
                )
        ));
    }
}
