package com.github.karixdev.webscraperservice.application.scraper;

import com.github.karixdev.webscraperservice.application.props.PlanPolslScrapperProperties;
import com.github.karixdev.webscraperservice.domain.RawAnchor;
import com.github.karixdev.webscraperservice.domain.RawCourse;
import com.github.karixdev.webscraperservice.domain.RawSchedule;
import com.github.karixdev.webscraperservice.domain.RawTimeInterval;
import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class PlanPolslResponseContentScraper {

    private final ElementAttributesScraper attributesScraper;
    private final CSSPropertiesScraper cssPropertiesScraper;

    public RawSchedule scrapSchedule(PlanPolslResponse planPolslResponse) {
        Document content = planPolslResponse.content();

        Set<Element> timeIntervalCells = getRawTimeIntervalCells(content);
        Set<Element> courseCells = getCourseCells(content);

        Set<RawTimeInterval> timeIntervals = getRawTimeIntervals(timeIntervalCells);
        Set<RawCourse> courses = getRawCourses(courseCells);

        return RawSchedule.builder()
                .timeIntervals(timeIntervals)
                .courses(courses)
                .build();
    }

    private Set<Element> getRawTimeIntervalCells(Document content) {
        return content.getElementsByClass(PlanPolslScrapperProperties.TIME_CELL_CLASS).stream()
                .filter(element -> PlanPolslScrapperProperties.TIME_CELL_TEXT_PATTERN.matcher(element.text()).matches())
                .collect(Collectors.toSet());
    }

    private Set<RawTimeInterval> getRawTimeIntervals(Set<Element> timeIntervalCells) {
        return timeIntervalCells.stream()
                .map(this::getRawTimeInterval)
                .collect(Collectors.toSet());
    }

    private RawTimeInterval getRawTimeInterval(Element timeIntervalCell) {
        String[] split = timeIntervalCell.text().split("-");
        return new RawTimeInterval(split[0], split[1]);
    }

    private Set<Element> getCourseCells(Document content) {
        Elements elements = content.getElementsByClass(PlanPolslScrapperProperties.COURSE_CELL_CLASS);
        return Set.copyOf(elements);
    }

    private Set<RawCourse> getRawCourses(Set<Element> courseCells) {
        return courseCells.stream()
                .map(this::getRawCourse)
                .collect(Collectors.toSet());
    }

    private RawCourse getRawCourse(Element courseCell) {
        int cw = attributesScraper.getCW(courseCell);
        int ch = attributesScraper.getCH(courseCell);

        int left = cssPropertiesScraper.getLeft(courseCell);
        int top = cssPropertiesScraper.getTop(courseCell);

        Set<RawAnchor> anchors = getRawCourseRawAnchors(courseCell);
        String text = getRawCourseText(courseCell);

        return RawCourse.builder()
                .text(text)
                .anchors(anchors)
                .height(ch)
                .width(cw)
                .left(left)
                .top(top)
                .build();
    }

    private Set<RawAnchor> getRawCourseRawAnchors(Element courseCell) {
        return courseCell.getElementsByTag("a")
                .stream()
                .map(this::getRawAnchor)
                .collect(Collectors.toSet());
    }

    private RawAnchor getRawAnchor(Element element) {
        return RawAnchor.builder()
                .text(element.text())
                .address(attributesScraper.getHref(element))
                .build();
    }

    private String getRawCourseText(Element courseCell) {
        courseCell.getElementsByTag("a")
                .forEach(Node::remove);

        return courseCell.wholeText().trim();
    }

}
