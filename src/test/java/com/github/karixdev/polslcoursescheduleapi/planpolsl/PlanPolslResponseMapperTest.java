package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import com.github.karixdev.polslcoursescheduleapi.frontend.CssService;
import com.github.karixdev.polslcoursescheduleapi.frontend.HtmlService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.TimeCell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanPolslResponseMapperTest {
    @InjectMocks
    PlanPolslResponseMapper underTest;

    @Mock
    CssService cssService;

    @Mock
    HtmlService htmlService;

    @Test
    void GivenDocumentWithTimeCellContainingTooShortText_WhenGetTimeCells_ThenReturnsEmptyList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="cd">abcd</div>
                """);

        // When
        List<TimeCell> result = underTest.getTimeCells(document);

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
        List<TimeCell> result = underTest.getTimeCells(document);

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
        List<TimeCell> result = underTest.getTimeCells(document);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("07:00-08:00");
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidStyles_WhenGetCourseCells_ThenReturnsEmptyList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="" cw="10" ch="10">
                    This is course div
                </div>
                """);

        when(htmlService.getSizeAttributeValue(any(), any()))
                .thenReturn(10);

        when(htmlService.getStyles(any()))
                .thenReturn(Map.of());

        when(cssService.getSizePropertyValue(any(), any()))
                .thenReturn(0);

        // When
        List<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenDocumentWithCourseCellWithInvalidCwAndChAttrs_WhenGetCourseCells_ThenReturnsEmptyList() {
        // Given
        Document document = Jsoup.parse("""
                <div class="coursediv" styles="left: 10px; top: 10px;" cw="0" ch="0">
                    This is course div
                </div>
                """);

        when(htmlService.getSizeAttributeValue(any(), any()))
                .thenReturn(0);

        when(htmlService.getStyles(any()))
                .thenReturn(Map.of(
                        "left", "10",
                        "top", "10"
                ));

        when(cssService.getSizePropertyValue(any(), any()))
                .thenReturn(10);

        // When
        List<CourseCell> result = underTest.getCourseCells(document);

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

        when(htmlService.getSizeAttributeValue(any(), eq("ch")))
                .thenReturn(10);

        when(htmlService.getSizeAttributeValue(any(), eq("cw")))
                .thenReturn(20);

        when(htmlService.getStyles(any()))
                .thenReturn(Map.of(
                        "left", "40",
                        "top", "30"
                ));

        when(cssService.getSizePropertyValue(any(), eq("top")))
                .thenReturn(30);


        when(cssService.getSizePropertyValue(any(), eq("left")))
                .thenReturn(40);

        // When
        List<CourseCell> result = underTest.getCourseCells(document);

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

        when(htmlService.getSizeAttributeValue(any(), eq("ch")))
                .thenReturn(10);

        when(htmlService.getSizeAttributeValue(any(), eq("cw")))
                .thenReturn(20);

        when(htmlService.getStyles(any()))
                .thenReturn(Map.of(
                        "left", "40",
                        "top", "30"
                ));

        when(cssService.getSizePropertyValue(any(), eq("top")))
                .thenReturn(30);


        when(cssService.getSizePropertyValue(any(), eq("left")))
                .thenReturn(40);

        // When
        List<CourseCell> result = underTest.getCourseCells(document);

        // Then
        assertThat(result).hasSize(1);

        CourseCell courseCell = result.get(0);

        assertThat(courseCell.getCh()).isEqualTo(10);
        assertThat(courseCell.getCw()).isEqualTo(20);
        assertThat(courseCell.getText()).isEqualTo("This is course div");
        assertThat(courseCell.getTop()).isEqualTo(30);
        assertThat(courseCell.getLeft()).isEqualTo(40);
    }
}
