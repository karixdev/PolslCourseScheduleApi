package com.github.karixdev.courseservice.application.command.handler;

import com.github.karixdev.courseservice.application.command.DeleteCourseByIdCommand;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.CourseWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteCourseByIdCommandHandler implements CommandHandler<DeleteCourseByIdCommand> {

    private final CourseRepository repository;
    private final TransactionManager transactionManager;

    @Override
    public void handle(DeleteCourseByIdCommand command) {
        Course course = repository.findById(command.id())
                .orElseThrow(() -> new CourseWithIdNotFoundException(command.id()));

        transactionManager.execute(() -> repository.delete(course));
    }

}
