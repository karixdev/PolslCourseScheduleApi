package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRequestCourseTypeToCourseTypeMapperTest {

    CourseRequestCourseTypeToCourseTypeMapper underTest = new CourseRequestCourseTypeToCourseTypeMapper();

    @ParameterizedTest
    @MethodSource("courseTypes")
    void GivenInput_WhenMap_ThenReturnsCorrectOutput(CourseRequestCourseType requestCourseType, CourseType expected) {
        // When
        CourseType result = underTest.map(requestCourseType);

        // Then
        assertThat(result).isEqualTo(expected);
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

}