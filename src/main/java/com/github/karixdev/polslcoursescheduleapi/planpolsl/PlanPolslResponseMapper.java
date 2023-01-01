package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import com.github.karixdev.polslcoursescheduleapi.frontend.CssService;
import com.github.karixdev.polslcoursescheduleapi.frontend.HtmlService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.TimeCell;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlanPolslResponseMapper {
    private final CssService cssService;
    private final HtmlService htmlService;

    public List<TimeCell> getTimeCells(Document document) {
        return document.getElementsByClass("CD").stream()
                .map(element -> new TimeCell(element.text()))
                .filter(this::isValidTimeCell)
                .toList();
    }

    private boolean isValidTimeCell(TimeCell timeCell) {
        String text = timeCell.getText();

        if (text.length() != 11) {
            return false;
        }

        Pattern pattern = Pattern.compile(
                "[0-9]+:[0-9]+-[0-9]+:[0-9]+",
                Pattern.CASE_INSENSITIVE);

        return pattern.matcher(text).matches();
    }

    public List<CourseCell> getCourseCells(Document document) {
        return document.getElementsByClass("coursediv").stream()
                .map(element -> {
                    Map<String, String> styles = htmlService.getStyles(element);

                    int top = cssService.getSizePropertyValue(styles, "top");
                    int left = cssService.getSizePropertyValue(styles, "left");

                    int ch = htmlService.getSizeAttributeValue(element, "ch");
                    int cw = htmlService.getSizeAttributeValue(element, "cw");

                    return new CourseCell(top, left, ch, cw, element.text());
                })
                .filter(this::isValidCourseCell)
                .toList();
    }

    private boolean isValidCourseCell(CourseCell courseCell) {
        return !courseCell.getText().isEmpty() &&
                courseCell.getTop() > 0 &&
                courseCell.getLeft() > 0 &&
                courseCell.getCh() > 0 &&
                courseCell.getCw() > 0;
    }
}
