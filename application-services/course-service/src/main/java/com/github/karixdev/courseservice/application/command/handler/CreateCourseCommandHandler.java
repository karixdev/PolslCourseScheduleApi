package com.github.karixdev.courseservice.application.command.handler;

import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateCourseCommandHandler implements CommandHandler<CreateCourseCommand> {

    private final CourseRepository repository;
    private final TransactionManager transactionManager;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public void handle(CreateCourseCommand command) {
        UUID scheduleId = command.scheduleId();
        if (Boolean.FALSE.equals(scheduleServiceClient.doesScheduleWithIdExist(scheduleId))) {
            throw new ScheduleWithIdNotFoundException(scheduleId);
        }

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .scheduleId(command.scheduleId())
                .startsAt(command.startsAt())
                .endsAt(command.endsAt())
                .name(command.name())
                .courseType(command.courseType())
                .teachers(command.teachers())
                .dayOfWeek(command.dayOfWeek())
                .weekType(command.weekType())
                .classrooms(command.classrooms())
                .additionalInfo(command.additionalInfo())
                .build();

        transactionManager.execute(() -> repository.save(course));
    }
}
