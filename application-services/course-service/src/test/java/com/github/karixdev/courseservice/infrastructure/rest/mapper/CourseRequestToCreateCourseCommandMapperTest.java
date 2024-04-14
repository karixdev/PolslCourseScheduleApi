package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRequestToCreateCourseCommandMapperTest {

    CourseRequestToCreateCourseCommandMapper underTest = new CourseRequestToCreateCourseCommandMapper();

    @Test
    void GivenInput_WhenMap_ThenReturnsCorrectOutputModel() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        CourseRequest course = CourseRequest.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        // When
        CreateCourseCommand result = underTest.map(course);

        // Then
        CreateCourseCommand expected = CreateCourseCommand.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("courseTypes")
    void GivenInput_WhenMap_ThenReturnsModelWithCorrectCourseType(CourseRequestCourseType requestCourseType, CourseType expectedCourseType) {
        // Given
        CourseRequest course = CourseRequest.builder()
                .courseType(requestCourseType)
                .build();

        // When
        CreateCourseCommand result = underTest.map(course);

        // Then
        assertThat(result.courseType()).isEqualTo(expectedCourseType);
    }

    @ParameterizedTest
    @MethodSource("weekTypes")
    void GivenInput_WhenMap_ThenReturnsModelWithCorrectWeekType(CourseRequestWeekType requestWeekType, WeekType expectedWeekType) {
        // Given
        CourseRequest course = CourseRequest.builder()
                .weekType(requestWeekType)
                .build();

        // When
        CreateCourseCommand result = underTest.map(course);

        // Then
        assertThat(result.weekType()).isEqualTo(expectedWeekType);
    }

    private static Stream<Arguments> courseTypes() {
        return Stream.of(
                Arguments.of(CourseRequestCourseType.LECTURE, CourseType.LECTURE),
                Arguments.of(CourseRequestCourseType.LAB, CourseType.LAB),
                Arguments.of(CourseRequestCourseType.PROJECT, CourseType.PROJECT),
                Arguments.of(CourseRequestCourseType.PRACTICAL, CourseType.PRACTICAL),
                Arguments.of(CourseRequestCourseType.INFO, CourseType.INFO)
        );
    }

    private static Stream<Arguments> weekTypes() {
        return Stream.of(
                Arguments.of(CourseRequestWeekType.EVEN, WeekType.EVEN),
                Arguments.of(CourseRequestWeekType.EVERY, WeekType.EVERY),
                Arguments.of(CourseRequestWeekType.ODD, WeekType.ODD)
        );
    }

}