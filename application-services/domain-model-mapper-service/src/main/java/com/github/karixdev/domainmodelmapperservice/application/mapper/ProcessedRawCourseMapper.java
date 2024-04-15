package com.github.karixdev.domainmodelmapperservice.application.mapper;

import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.processed.CourseType;
import com.github.karixdev.domainmodelmapperservice.domain.processed.WeekType;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawAnchor;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawCourse;
import com.github.karixdev.domainmodelmapperservice.application.props.CourseMapperProperties;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProcessedRawCourseMapper {

    public ProcessedRawCourse map(RawCourse rawCourse, UUID scheduleId, LocalTime startTime) {
        int scheduleStartTimeHour = startTime.getHour();

        LocalTime startsAt = getTime(
                rawCourse.top(),
                scheduleStartTimeHour,
                false
        );
        LocalTime endsAt = getTime(
                rawCourse.top() + rawCourse.height(),
                scheduleStartTimeHour,
                true
        );

        CourseType courseType = getCourseType(rawCourse.text());
        String name = getName(rawCourse.text());

        DayOfWeek dayOfWeek = getDayOfWeek(rawCourse.left());
        WeekType weeks = getWeekType(rawCourse.left(), rawCourse.width(), dayOfWeek);

        String teachers = getTeachers(rawCourse.anchors());
        String rooms = getRooms(rawCourse.anchors());

        String additionalInfo = getAdditionalInfo(rawCourse.text());

        return ProcessedRawCourse.builder()
                .scheduleId(scheduleId)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .name(name)
                .courseType(courseType)
                .teachers(teachers)
                .dayOfWeek(dayOfWeek)
                .weekType(weeks)
                .classroom(rooms)
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

    private String getTeachers(Set<RawAnchor> anchors) {
        return String.join(", ", getTextFromLinks(
                anchors,
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

    private String getRooms(Set<RawAnchor> anchors) {
        return String.join(", ", getTextFromLinks(
                anchors,
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

    private Set<String> getTextFromLinks(Set<RawAnchor> anchors, String type) {
        if (anchors == null) {
            return Set.of();
        }

        return anchors.stream()
                .filter(anchor -> getTypeFromUrl(anchor.address()).equals(type))
                .map(RawAnchor::text)
                .collect(Collectors.toSet());
    }

}
