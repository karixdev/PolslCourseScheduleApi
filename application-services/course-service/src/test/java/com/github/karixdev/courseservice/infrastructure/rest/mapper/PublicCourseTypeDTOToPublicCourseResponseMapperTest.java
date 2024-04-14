package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseCourseType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PublicCourseTypeDTOToPublicCourseResponseMapperTest {

    PublicCourseTypeDTOToPublicCourseResponseCourseTypeMapper underTest = new PublicCourseTypeDTOToPublicCourseResponseCourseTypeMapper();

    @ParameterizedTest
    @MethodSource("courseTypes")
    void GivenInput_WhenMap_ThenReturnsCorrectOutput(PublicCourseTypeDTO requestCourseType, PublicCourseResponseCourseType expected) {
        // When
        PublicCourseResponseCourseType result = underTest.map(requestCourseType);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> courseTypes() {
        return Stream.of(
                Arguments.of(PublicCourseTypeDTO.LECTURE, PublicCourseResponseCourseType.LECTURE),
                Arguments.of(PublicCourseTypeDTO.LAB, PublicCourseResponseCourseType.LAB),
                Arguments.of(PublicCourseTypeDTO.PROJECT, PublicCourseResponseCourseType.PROJECT),
                Arguments.of(PublicCourseTypeDTO.PRACTICAL, PublicCourseResponseCourseType.PRACTICAL),
                Arguments.of(PublicCourseTypeDTO.INFO, PublicCourseResponseCourseType.INFO)
        );
    }

}