package com.github.karixdev.courseservice.application.validator;

import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProcessedRawCourseValidator implements Validator<ProcessedRawCourse> {

    @Override
    public boolean isValid(ProcessedRawCourse model) {
        return Objects.nonNull(model.scheduleId())
                && Objects.nonNull(model.name())
                && Objects.nonNull(model.courseType())
                && Objects.nonNull(model.dayOfWeek())
                && Objects.nonNull(model.weekType())
                && Objects.nonNull(model.startsAt())
                && Objects.nonNull(model.endsAt());
    }

}
