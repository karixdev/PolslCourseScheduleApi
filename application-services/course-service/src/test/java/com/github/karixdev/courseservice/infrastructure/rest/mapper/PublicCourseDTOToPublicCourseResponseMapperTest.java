package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponse;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseWeekType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PublicCourseDTOToPublicCourseResponseMapperTest {

    PublicCourseDTOToPublicCourseResponseMapper underTest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        underTest = new PublicCourseDTOToPublicCourseResponseMapper(
                (ModelMapper<PublicCourseWeekTypeDTO, PublicCourseResponseWeekType>) mock(ModelMapper.class),
                (ModelMapper<PublicCourseTypeDTO, PublicCourseResponseCourseType>) mock(ModelMapper.class)
        );
    }

    @Test
    void GivenInput_WhenMap_ThenReturnsCorrectOutputModel() {
        // Given
        UUID id = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        PublicCourseDTO course = PublicCourseDTO.builder()
                .id(id)
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
        PublicCourseResponse result = underTest.map(course);

        // Then
        PublicCourseResponse expected = PublicCourseResponse.builder()
                .id(id)
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("courseType", "weekType")
                .isEqualTo(expected);
    }

}