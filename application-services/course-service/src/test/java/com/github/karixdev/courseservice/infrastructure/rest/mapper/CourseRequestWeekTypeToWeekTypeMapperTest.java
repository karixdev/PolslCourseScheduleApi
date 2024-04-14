package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRequestWeekTypeToWeekTypeMapperTest {

    CourseRequestWeekTypeToWeekTypeMapper underTest = new CourseRequestWeekTypeToWeekTypeMapper();

    @ParameterizedTest
    @MethodSource("weekTypes")
    void GivenInput_WhenMap_ThenReturnsCorrectOutput(CourseRequestWeekType requestWeekType, WeekType expected) {
        // When
        WeekType result = underTest.map(requestWeekType);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> weekTypes() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(CourseRequestWeekType.EVEN, WeekType.EVEN),
                Arguments.of(CourseRequestWeekType.EVERY, WeekType.EVERY),
                Arguments.of(CourseRequestWeekType.ODD, WeekType.ODD)
        );
    }

}