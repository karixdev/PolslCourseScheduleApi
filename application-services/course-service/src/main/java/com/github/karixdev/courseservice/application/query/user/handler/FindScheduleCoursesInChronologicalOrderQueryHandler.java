package com.github.karixdev.courseservice.application.query.user.handler;

import com.github.karixdev.courseservice.application.comparator.CourseChronologicalOrderComparatorComparator;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.query.handler.QueryHandler;
import com.github.karixdev.courseservice.application.query.user.FindScheduleCoursesInChronologicalOrderQuery;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindScheduleCoursesInChronologicalOrderQueryHandler implements QueryHandler<FindScheduleCoursesInChronologicalOrderQuery, List<PublicCourseDTO>> {

    private final CourseRepository repository;
    private final CourseChronologicalOrderComparatorComparator comparator;
    private final ModelMapper<Course, PublicCourseDTO> mapper;

    @Override
    public List<PublicCourseDTO> handle(FindScheduleCoursesInChronologicalOrderQuery query) {
        List<Course> courses = repository.findByScheduleId(query.scheduleId());

        return courses.stream()
                .sorted(comparator)
                .map(mapper::map)
                .toList();
    }

}
