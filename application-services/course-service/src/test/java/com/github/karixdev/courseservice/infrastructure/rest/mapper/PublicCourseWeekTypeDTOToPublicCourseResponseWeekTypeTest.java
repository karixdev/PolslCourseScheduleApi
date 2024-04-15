package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseWeekType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PublicCourseWeekTypeDTOToPublicCourseResponseWeekTypeTest {

    PublicCourseWeekTypeDTOToPublicCourseResponseWeekType underTest = new PublicCourseWeekTypeDTOToPublicCourseResponseWeekType();

    @ParameterizedTest
    @MethodSource("weekTypes")
    void GivenInput_WhenMap_ThenReturnsCorrectOutput(PublicCourseWeekTypeDTO requestWeekType, PublicCourseResponseWeekType expected) {
        // When
        PublicCourseResponseWeekType result = underTest.map(requestWeekType);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> weekTypes() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(PublicCourseWeekTypeDTO.EVEN, PublicCourseResponseWeekType.EVEN),
                Arguments.of(PublicCourseWeekTypeDTO.EVERY, PublicCourseResponseWeekType.EVERY),
                Arguments.of(PublicCourseWeekTypeDTO.ODD, PublicCourseResponseWeekType.ODD)
        );
    }

}