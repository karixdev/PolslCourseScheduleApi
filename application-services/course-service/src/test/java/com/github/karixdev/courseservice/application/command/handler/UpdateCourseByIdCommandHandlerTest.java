package com.github.karixdev.courseservice.application.command.handler;


import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.dal.TransactionCallback;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.CourseWithIdNotFoundException;
import com.github.karixdev.courseservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import matcher.CourseWholeEntityArgumentMatcher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static matcher.CourseWholeEntityArgumentMatcher.courseWholeEntityEq;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCourseByIdCommandHandlerTest {

    @InjectMocks
    UpdateCourseByIdCommandHandler underTest;

    @Mock
    CourseRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Mock
    ScheduleServiceClient scheduleServiceClient;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Test
    void GivenCommandWithNonExistingId_WhenHandle_ThenThrowsCourseWithIdNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        UpdateCourseByIdCommand command = UpdateCourseByIdCommand.builder()
                .id(id)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Given & When
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(CourseWithIdNotFoundException.class);
    }

    @Test
    void GivenCommandWithNewNonScheduleId_WhenHandle_ThenThrowsScheduleWithIdNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        UUID newScheduleId = UUID.randomUUID();

        UpdateCourseByIdCommand command = UpdateCourseByIdCommand.builder()
                .id(id)
                .scheduleId(newScheduleId)
                .build();

        Course course = Course.builder()
                .id(id)
                .scheduleId(UUID.randomUUID())
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(course));

        when(scheduleServiceClient.doesScheduleWithIdExist(newScheduleId))
                .thenReturn(false);

        // Given & When
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(ScheduleWithIdNotFoundException.class);
    }

    @Test
    void GivenValidCommand_WhenHandle_ThenSavesUpdatedSchedule() {
        // Given
        UUID id = UUID.randomUUID();
        UUID newScheduleId = UUID.randomUUID();

        UpdateCourseByIdCommand command = UpdateCourseByIdCommand.builder()
                .id(id)
                .scheduleId(newScheduleId)
                .startsAt(LocalTime.of(17, 32))
                .endsAt(LocalTime.of(19, 24))
                .name("course-name-2")
                .courseType(CourseType.LECTURE)
                .teachers("dr Tom")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.ODD)
                .classrooms("Lecture 2")
                .additionalInfo("Bring notes")
                .build();

        Course course = Course.builder()
                .id(id)
                .scheduleId(UUID.randomUUID())
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

        when(repository.findById(id))
                .thenReturn(Optional.of(course));

        when(scheduleServiceClient.doesScheduleWithIdExist(newScheduleId))
                .thenReturn(true);

        Course expected = Course.builder()
                .id(id)
                .scheduleId(newScheduleId)
                .startsAt(LocalTime.of(17, 32))
                .endsAt(LocalTime.of(19, 24))
                .name("course-name-2")
                .courseType(CourseType.LECTURE)
                .teachers("dr Tom")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.ODD)
                .classrooms("Lecture 2")
                .additionalInfo("Bring notes")
                .build();

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback callback = transactionCallbackCaptor.getValue();
        callback.execute();

        verify(repository).save(courseWholeEntityEq(expected));
    }

}