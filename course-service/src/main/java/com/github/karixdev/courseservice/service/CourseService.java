package com.github.karixdev.courseservice.service;

import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.exception.NotExistingScheduleException;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final ScheduleClient scheduleClient;

    @Transactional
    public CourseResponse create(CourseRequest request) {
        UUID scheduleId = request.getScheduleId();
        if (!doesScheduleExist(scheduleId)) {
            throw new NotExistingScheduleException(scheduleId);
        }

        Course course = Course.builder()
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .name(request.getName())
                .courseType(request.getCourseType())
                .teachers(request.getTeachers())
                .dayOfWeek(request.getDayOfWeek())
                .weekType(request.getWeekType())
                .classroom(request.getClassrooms())
                .additionalInfo(request.getAdditionalInfo())
                .scheduleId(request.getScheduleId())
                .build();

        repository.save(course);

        return mapper.map(course);
    }

    private boolean doesScheduleExist(UUID scheduleId) {
        return scheduleClient.findById(scheduleId).isPresent();
    }
}
