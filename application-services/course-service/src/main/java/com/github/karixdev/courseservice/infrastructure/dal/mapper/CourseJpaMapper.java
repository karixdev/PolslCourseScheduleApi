package com.github.karixdev.courseservice.infrastructure.dal.mapper;

import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import org.springframework.stereotype.Component;

@Component
public class CourseJpaMapper {

    public CourseEntity toJpa(Course input) {
        return CourseEntity.builder()
                .id(input.getId())
                .scheduleId(input.getScheduleId())
                .name(input.getName())
                .courseType(mapToJpaCourseType(input.getCourseType()))
                .teachers(input.getTeachers())
                .additionalInfo(input.getAdditionalInfo())
                .dayOfWeek(input.getDayOfWeek())
                .weekType(mapToJpaWeekType(input.getWeekType()))
                .startsAt(input.getStartsAt())
                .endsAt(input.getEndsAt())
                .classrooms(input.getClassrooms())
                .build();
    }

    private CourseEntityCourseType mapToJpaCourseType(CourseType courseType) {
        return switch (courseType) {
            case LECTURE -> CourseEntityCourseType.LECTURE;
            case LAB -> CourseEntityCourseType.LAB;
            case PROJECT -> CourseEntityCourseType.PROJECT;
            case PRACTICAL -> CourseEntityCourseType.PRACTICAL;
            case INFO -> CourseEntityCourseType.INFO;
        };
    }

    private CourseEntityWeekType mapToJpaWeekType(WeekType weekType) {
        return switch (weekType) {
            case ODD -> CourseEntityWeekType.ODD;
            case EVEN -> CourseEntityWeekType.EVEN;
            case EVERY -> CourseEntityWeekType.EVERY;
        };
    }

    public Course toDomain(CourseEntity input) {
        return Course.builder()
                .id(input.getId())
                .scheduleId(input.getScheduleId())
                .name(input.getName())
                .courseType(mapToDomainCourseType(input.getCourseType()))
                .teachers(input.getTeachers())
                .additionalInfo(input.getAdditionalInfo())
                .dayOfWeek(input.getDayOfWeek())
                .weekType(mapToDomainWeekType(input.getWeekType()))
                .startsAt(input.getStartsAt())
                .endsAt(input.getEndsAt())
                .classrooms(input.getClassrooms())
                .build();
    }

    private CourseType mapToDomainCourseType(CourseEntityCourseType courseType) {
        return switch (courseType) {
            case LECTURE -> CourseType.LECTURE;
            case LAB -> CourseType.LAB;
            case PROJECT -> CourseType.PROJECT;
            case PRACTICAL -> CourseType.PRACTICAL;
            case INFO -> CourseType.INFO;
        };
    }

    private WeekType mapToDomainWeekType(CourseEntityWeekType weekType) {
        return switch (weekType) {
            case ODD -> WeekType.ODD;
            case EVEN -> WeekType.EVEN;
            case EVERY -> WeekType.EVERY;
        };
    }

}
