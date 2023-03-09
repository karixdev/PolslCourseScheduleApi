package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.domain.CourseType;
import com.github.karixdev.webscraperservice.course.domain.Weeks;
import com.github.karixdev.webscraperservice.course.properties.CourseMapperProperties;
import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.Link;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseMapper {
    public Course map(CourseCell courseCell, LocalTime startTime) {
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
        Weeks weeks = getWeeks(courseCell.left(), courseCell.cw(), dayOfWeek);

        Set<String> teachers = getTeachers(courseCell.links());
        Set<String> rooms = getRooms(courseCell.links());

        String additionalInfo = getAdditionalInfo(courseCell.text());

        return new Course(
                startsAt,
                endsAt,
                name,
                courseType,
                teachers,
                dayOfWeek,
                weeks,
                rooms,
                additionalInfo
        );
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

        return switch (firstLineSplit[1].trim()) {
            case "ćw" -> CourseType.PRACTICAL;
            case "lab" -> CourseType.LAB;
            case "proj" -> CourseType.PROJECT;
            case "wyk" -> CourseType.LECTURE;
            default -> CourseType.INFO;
        };
    }

    private String getName(String text) {
        return getFirstLineSplit(text)[0].trim();
    }

    private String[] getFirstLineSplit(String text) {
        String[] linesSplit = text.split("\n");

        return linesSplit[0].split(",");
    }

    private Set<String> getTeachers(Set<Link> links) {
        return links.stream()
                .filter(link -> getTypeFromUrl(link.href())
                        .equals(CourseMapperProperties.COURSE_LINK_TEACHER_TYPE)
                )
                .map(Link::text)
                .collect(Collectors.toSet());
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
        Map<Integer, DayOfWeek> leftValueMap = CourseMapperProperties.DAY_OF_WEEK_MAP;

        return leftValueMap.keySet().stream()
                .filter(key -> left == key + CourseMapperProperties.WEEK_CELL_HALF_OF_WIDTH || left == key)
                .findFirst()
                .map(leftValueMap::get)
                .orElseThrow();
    }

    private Set<String> getRooms(Set<Link> links) {
        return links.stream()
                .filter(link -> getTypeFromUrl(link.href())
                        .equals(CourseMapperProperties.COURSE_LINK_ROOM_TYPE)
                )
                .map(Link::text)
                .collect(Collectors.toSet());
    }

    private Weeks getWeeks(int left, int cw, DayOfWeek dayOfWeek) {
        if (cw == CourseMapperProperties.EVERY_WEEK_CW_VALUE) {
            return Weeks.EVERY;
        }

        boolean isOdd = CourseMapperProperties.DAY_OF_WEEK_MAP
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(left) && entry.getValue().equals(dayOfWeek));

        return isOdd ? Weeks.ODD : Weeks.EVEN;
    }

    private String getAdditionalInfo(String text) {
        int idx = text.indexOf(CourseMapperProperties.COURSE_ADDITIONAL_INFO_PREFIX);

        if (idx == -1) {
            return null;
        }

        return text.substring(idx).trim()
                .replaceAll("\n", " ")
                .replaceAll(" +", " ");
    }
}
