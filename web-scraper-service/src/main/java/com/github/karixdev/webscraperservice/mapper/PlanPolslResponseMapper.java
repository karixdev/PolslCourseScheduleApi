package com.github.karixdev.webscraperservice.mapper;

import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.course.raw.Link;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.props.PlanPolslMapperProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanPolslResponseMapper {

    private final DOMElementAttributesMapper attributesMapper;

    public PlanPolslResponse map(Document document) {
        Set<TimeCell> timeCells = getTimeCells(document);
        Set<CourseCell> courseCells = getCourseCells(document);

        return new PlanPolslResponse(timeCells, courseCells);
    }

    private Set<TimeCell> getTimeCells(Document document) {
        return document.getElementsByClass(PlanPolslMapperProperties.TIME_CELL_CLASS)
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
                "\\d+:\\d+-\\d+:\\d+",
                Pattern.CASE_INSENSITIVE);

        return pattern.matcher(text).matches();
    }

    private Set<CourseCell> getCourseCells(Document document) {
        return document.getElementsByClass(PlanPolslMapperProperties.COURSE_CELL_CLASS)
                .stream()
                .map(this::getCourseCell)
                .filter(this::isValidCourseCell)
                .collect(Collectors.toSet());
    }

    private CourseCell getCourseCell(Element element) {
        Map<String, String> styles = attributesMapper.getStylesAttr(element);

        int top = attributesMapper.getCssSizeProperty(styles, "top");
        int left = attributesMapper.getCssSizeProperty(styles, "left");

        int ch = attributesMapper.getSizeAttr(element, "ch");
        int cw = attributesMapper.getSizeAttr(element, "cw");

        Set<Link> links = getLinks(element);
        String text = getText(element);

        return new CourseCell(
                top,
                left,
                ch,
                cw,
                text,
                links
        );
    }

    private boolean isValidCourseCell(CourseCell courseCell) {
        return !courseCell.text().isEmpty() &&
                courseCell.top() > 0 &&
                courseCell.left() > 0 &&
                courseCell.ch() > 0 &&
                courseCell.cw() > 0;
    }

    private Set<Link> getLinks(Element element) {
        return element.getElementsByTag("a")
                .stream()
                .map(el -> new Link(
                        el.text(),
                        el.attr("href"))
                )
                .collect(Collectors.toSet());
    }

    private String getText(Element element) {
        element.getElementsByTag("a")
                .forEach(Node::remove);

        return element.wholeText().trim();
    }
}
