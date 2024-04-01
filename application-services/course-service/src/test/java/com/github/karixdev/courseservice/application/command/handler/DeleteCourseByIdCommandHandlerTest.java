package com.github.karixdev.courseservice.application.command.handler;

import com.github.karixdev.courseservice.application.command.DeleteCourseByIdCommand;
import com.github.karixdev.courseservice.application.dal.TransactionCallback;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.CourseWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCourseByIdCommandHandlerTest {

    @InjectMocks
    DeleteCourseByIdCommandHandler underTest;

    @Mock
    CourseRepository repository;

    @Mock
    TransactionManager transactionManager;

    @Captor
    ArgumentCaptor<TransactionCallback> transactionCallbackCaptor;

    @Test
    void GivenCommandWithNonExistingId_WhenHandle_ThenThrowsCourseWithIdNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();

        DeleteCourseByIdCommand command = DeleteCourseByIdCommand.builder()
                .id(id)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.handle(command))
                .isInstanceOf(CourseWithIdNotFoundException.class);
    }

    @Test
    void GivenValidCommand_WhenHandle_ThenDeletesEntity() {
        // Given
        UUID id = UUID.randomUUID();

        DeleteCourseByIdCommand command = DeleteCourseByIdCommand.builder()
                .id(id)
                .build();

        Course course = Course.builder()
                .id(id)
                .build();

        when(repository.findById(id))
                .thenReturn(Optional.of(course));

        // When
        underTest.handle(command);

        // Then
        verify(transactionManager).execute(transactionCallbackCaptor.capture());
        TransactionCallback callback = transactionCallbackCaptor.getValue();
        callback.execute();

        verify(repository).delete(course);
    }

}