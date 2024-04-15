package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CourseRequestToUpdateCourseByIdCommandMapperTest {

    CourseRequestToUpdateCourseByIdCommandMapper underTest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        underTest = new CourseRequestToUpdateCourseByIdCommandMapper(
                (ModelMapper<CourseRequestCourseType, CourseType>) mock(ModelMapper.class),
                (ModelMapper<CourseRequestWeekType, WeekType>) mock(ModelMapper.class)
        );
    }

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

        UUID id = UUID.randomUUID();
        Map<String, Object> attrs = Map.of("id", id);

        // When
        UpdateCourseByIdCommand result = underTest.map(course, attrs);

        // Then
        UpdateCourseByIdCommand expected = UpdateCourseByIdCommand.builder()
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