package com.github.karixdev.webscraperservice.planpolsl;

import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import com.github.karixdev.webscraperservice.planpolsl.properties.PlanPolslAdapterProperties;
import com.github.karixdev.webscraperservice.shared.service.HtmlElementService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanPolslAdapter {
    private final HtmlElementService htmlElementService;
    public Set<TimeCell> getTimeCells(Document document) {
        return document.getElementsByClass(PlanPolslAdapterProperties.TIME_CELL_CLASS)
                .stream()
                .map(element -> new TimeCell(element.text()))
                .filter(this::isValidTimeCell)
                .collect(Collectors.toSet());
    }

    private boolean isValidTimeCell(TimeCell timeCell) {
        String text = timeCell.text();

        if (text.length() != 11) {
            return false;
        }

        Pattern pattern = Pattern.compile(
                "[0-9]+:[0-9]+-[0-9]+:[0-9]+",
                Pattern.CASE_INSENSITIVE);

        return pattern.matcher(text).matches();
    }

    public Set<CourseCell> getCourseCells(Document document) {
        return document.getElementsByClass(PlanPolslAdapterProperties.COURSE_CELL_CLASS)
                .stream()
                .map(this::getCourseCell)
                .filter(this::isValidCourseCell)
                .collect(Collectors.toSet());
    }

    private CourseCell getCourseCell(Element element) {
        Map<String, String> styles = htmlElementService.getStylesAttr(element);

        int top = htmlElementService.getCssSizeProperty(styles, "top");
        int left = htmlElementService.getCssSizeProperty(styles, "left");

        int ch = htmlElementService.getSizeAttr(element, "ch");
        int cw = htmlElementService.getSizeAttr(element, "cw");

        return new CourseCell(top, left, ch, cw, element.wholeText().trim());
    }

    private boolean isValidCourseCell(CourseCell courseCell) {
        return !courseCell.text().isEmpty() &&
                courseCell.top() > 0 &&
                courseCell.left() > 0 &&
                courseCell.ch() > 0 &&
                courseCell.cw() > 0;
    }
}
