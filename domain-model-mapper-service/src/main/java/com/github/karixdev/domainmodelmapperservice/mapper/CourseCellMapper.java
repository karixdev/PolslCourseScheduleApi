package com.github.karixdev.domainmodelmapperservice.mapper;

import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseType;
import com.github.karixdev.commonservice.model.course.domain.WeekType;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.course.raw.Link;
import com.github.karixdev.domainmodelmapperservice.props.CourseMapperProperties;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseCellMapper {

    public CourseDomain mapToCourse(CourseCell courseCell, LocalTime startTime) {
        int scheduleStartTimeHour = startTime.getHour();

        LocalTime startsAt = getTime(
                courseCell.top(),
                scheduleStartTimeHour,
                false
        );
        LocalTime endsAt = getTime(
                courseCell.top() + courseCell.ch(),
                scheduleStartTimeHour,
                true
        );

        CourseType courseType = getCourseType(courseCell.text());
        String name = getName(courseCell.text());

        DayOfWeek dayOfWeek = getDayOfWeek(courseCell.left());
        WeekType weeks = getWeekType(courseCell.left(), courseCell.cw(), dayOfWeek);

        String teachers = getTeachers(courseCell.links());
        String rooms = getRooms(courseCell.links());

        String additionalInfo = getAdditionalInfo(courseCell.text());

        return CourseDomain.builder()
                .startsAt(startsAt)
                .endsAt(endsAt)
                .name(name)
                .courseType(courseType)
                .teachers(teachers)
                .dayOfWeek(dayOfWeek)
                .weeks(weeks)
                .classrooms(rooms)
                .additionalInfo(additionalInfo)
                .build();
    }

    private LocalTime getTime(int top, int startsAt, boolean addBorderToTop) {
        if (addBorderToTop) {
            top += CourseMapperProperties.COURSE_CELL_BORDER_SIZE;
        }

        int difference = top - CourseMapperProperties.FIRST_CELL_TOP_VALUE;
        double ratio = difference / CourseMapperProperties.ONE_HOUR_CELL_HEIGHT;
        ratio /= 0.25;

        int totalNumOfQuarters = (int) Math.ceil(ratio);
        totalNumOfQuarters *= 15;

        double totalTime = totalNumOfQuarters / 60.0 + startsAt;
        int hours = (int) totalTime;
        int minutes = (int) ((totalTime - hours) * 60);

        return LocalTime.of(hours, minutes);
    }

    private CourseType getCourseType(String text) {
        String[] firstLineSplit = getFirstLineSplit(text);

        if (firstLineSplit.length == 1) {
            return CourseType.INFO;
        }

        String typeName = firstLineSplit[1].trim();

        return Optional
                .ofNullable(CourseMapperProperties.COURSE_TYPE_MAP.get(typeName))
                .orElse(CourseType.INFO);
    }

    private String getName(String text) {
        return getFirstLineSplit(text)[0].trim();
    }

    private String[] getFirstLineSplit(String text) {
        String[] linesSplit = text.split("\n");

        return linesSplit[0].split(",");
    }

    private String getTeachers(Set<Link> links) {
        return String.join(", ", getTextFromLinks(
                links,
                CourseMapperProperties.COURSE_LINK_TEACHER_TYPE)
        );
    }

    private String getTypeFromUrl(String href) {
        int len = CourseMapperProperties.COURSE_LINKS_PREFIX.length();
        String str = href.substring(len + 1);

        int typeIdx = str.indexOf("type");

        if (typeIdx == -1) {
            return "";
        }

        String substr = str.substring(typeIdx);

        if (substr.contains("&")) {
            int typeEndIdx = substr.indexOf("&");
            substr = substr.substring(typeIdx, typeEndIdx);
        }

        return substr.split("=")[1];
    }

    private DayOfWeek getDayOfWeek(int left) {
        if (CourseMapperProperties.DAY_OF_WEEK_MAP.containsKey(left)) {
            return CourseMapperProperties.DAY_OF_WEEK_MAP.get(left);
        }

        return CourseMapperProperties.DAY_OF_WEEK_MAP.get(left - CourseMapperProperties.WEEK_CELL_HALF_OF_WIDTH);
    }

    private String getRooms(Set<Link> links) {
        return String.join(", ", getTextFromLinks(
                links,
                CourseMapperProperties.COURSE_LINK_ROOM_TYPE)
        );
    }

    private WeekType getWeekType(int left, int cw, DayOfWeek dayOfWeek) {
        if (cw == CourseMapperProperties.EVERY_WEEK_CW_VALUE) {
            return WeekType.EVERY;
        }

        boolean isOdd = CourseMapperProperties.DAY_OF_WEEK_MAP
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(left) && entry.getValue().equals(dayOfWeek));

        return isOdd ? WeekType.ODD : WeekType.EVEN;
    }

    private String getAdditionalInfo(String text) {
        int idx = text.indexOf(CourseMapperProperties.COURSE_ADDITIONAL_INFO_PREFIX);

        if (idx == -1) {
            return null;
        }

        return text.substring(idx).trim()
                .replace("\n", " ")
                .replace(" +", " ");
    }

    private Set<String> getTextFromLinks(Set<Link> links, String type) {
        return links.stream()
                .filter(link -> getTypeFromUrl(link.href()).equals(type))
                .map(Link::text)
                .collect(Collectors.toSet());
    }

}
