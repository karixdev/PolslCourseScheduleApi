package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.dto.CourseCell;
import com.github.karixdev.webscraperservice.dto.TimeCell;
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
public class PlanPolslAdapterTest {
    @InjectMocks
    PlanPolslAdapter underTest;

    @Mock
    HtmlElementService htmlElementService;

    @Test
    void GivenDocumentWithTimeCellContainingTooShortText_WhenGetTimeCells_ThenReturnsEmptyList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">abcd</div>
                """);

        // When
        Set<TimeCell> result = underTest.getTimeCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithTimeCellContainingTextNotMatchingRegex_WhenGetTimeCells_ThenReturnsEmptyList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">ab:cd-ef:gh</div>
                """);

        // When
        Set<TimeCell> result = underTest.getTimeCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithValidTimeCell_WhenGetTimeCells_ThenReturnsProperList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">07:00-08:00</div>
                """);

        // When
        Set<TimeCell> result = underTest.getTimeCells(document);

        // Then
        assertThat(result).isEqualTo(Set.of(
                new TimeCell("07:00-08:00")
        ));
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidStyles_WhenGetCourseCells_ThenReturnsEmptyList() {
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
        Set<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidCwAndChAttrs_WhenGetCourseCells_ThenReturnsEmptyList() {
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
        Set<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithCourseCellWithEmptyText_WhenGetCourseCells_ThenReturnsEmptyList() {
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
        Set<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithValidCourseCell_WhenGetCourseCells_ThenReturnsProperList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="left: 40px; top: 30px;" cw="20" ch="10">
                    This is course div
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
        Set<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).isEqualTo(Set.of(
                new CourseCell(
                        30,
                        40,
                        10,
                        20,
                        "This is course div"
                )
        ));
    }
}
