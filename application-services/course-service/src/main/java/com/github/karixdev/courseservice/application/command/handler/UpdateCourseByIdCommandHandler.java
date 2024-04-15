package com.github.karixdev.courseservice.application.command.handler;

import com.github.karixdev.courseservice.application.client.ScheduleServiceClient;
import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.application.exception.CourseWithIdNotFoundException;
import com.github.karixdev.courseservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.github.karixdev.courseservice.application.utils.BooleanUtils.isFalse;

@Component
@RequiredArgsConstructor
public class UpdateCourseByIdCommandHandler implements CommandHandler<UpdateCourseByIdCommand> {

    private final CourseRepository repository;
    private final TransactionManager transactionManager;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public void handle(UpdateCourseByIdCommand command) {
        Course course = repository.findById(command.id())
                .orElseThrow(() -> new CourseWithIdNotFoundException(command.id()));

        UUID newScheduleId = command.scheduleId();
        UUID oldScheduleId = course.getScheduleId();

        if (!oldScheduleId.equals(newScheduleId) && isFalse(scheduleServiceClient.doesScheduleWithIdExist(newScheduleId))) {
            throw new ScheduleWithIdNotFoundException(newScheduleId);
        }

        course.setScheduleId(newScheduleId);
        course.setStartsAt(command.startsAt());
        course.setEndsAt(command.endsAt());
        course.setName(command.name());
        course.setCourseType(command.courseType());
        course.setTeachers(command.teachers());
        course.setDayOfWeek(command.dayOfWeek());
        course.setWeekType(command.weekType());
        course.setClassrooms(command.classrooms());
        course.setAdditionalInfo(command.additionalInfo());

        transactionManager.execute(() -> repository.save(course));
    }
}
