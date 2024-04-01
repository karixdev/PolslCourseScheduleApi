package com.github.karixdev.courseservice.application.command.handler;

import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.dal.TransactionCallback;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static matcher.CourseNonIdArgumentMatcher.courseNonIdEq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCourseCommandHandlerTest {

    @InjectMocks
    CreateCourseCommandHandler underTest;

    @Mock
    CourseRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Mock
    ScheduleServiceClient scheduleServiceClient;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Captor
    ArgumentCaptor<Course> courseCaptor;

    @Test
    void GivenCommandWithNotExistingScheduleId_WhenHandle_ThenThrowsScheduleWithIdNotFoundException() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        CreateCourseCommand command = CreateCourseCommand.builder()
                .scheduleId(scheduleId)
                .build();

        when(scheduleServiceClient.doesScheduleWithIdExist(scheduleId))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(ScheduleWithIdNotFoundException.class);
    }

    @Test
    void GivenValidCommand_WhenHandle_ThenSavesCorrectEntity() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        CreateCourseCommand command = CreateCourseCommand.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();


        Course expected = Course.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        when(scheduleServiceClient.doesScheduleWithIdExist(scheduleId))
                .thenReturn(true);

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback callback = transactionCallbackCaptor.getValue();
        callback.execute();

        verify(repository).save(courseNonIdEq(expected));
    }

}