package com.github.karixdev.courseservice.service;

import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.comparator.CourseComparator;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.exception.NotExistingScheduleException;
import com.github.karixdev.courseservice.exception.ResourceNotFoundException;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.producer.CourseEventProducer;
import com.github.karixdev.courseservice.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final ScheduleClient scheduleClient;
    private final CourseComparator courseComparator;
    private final CourseEventProducer producer;

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

        producer.produceCoursesUpdate(scheduleId);

        return mapper.map(course);
    }

    private boolean doesScheduleExist(UUID scheduleId) {
        return scheduleClient.findById(scheduleId).isPresent();
    }

    private Course findByIdOrElseThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("Course with it %s not found".formatted(id));
        });
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        Course course = findByIdOrElseThrow(id);

        UUID newScheduleId = request.getScheduleId();
        if (!course.getScheduleId().equals(request.getScheduleId())
                && !doesScheduleExist(newScheduleId)
        ) {
            throw new NotExistingScheduleException(newScheduleId);
        }

        course.setScheduleId(newScheduleId);
        course.setStartsAt(request.getStartsAt());
        course.setEndsAt(request.getEndsAt());
        course.setName(request.getName());
        course.setCourseType(request.getCourseType());
        course.setTeachers(request.getTeachers());
        course.setDayOfWeek(request.getDayOfWeek());
        course.setWeekType(request.getWeekType());
        course.setClassroom(request.getClassrooms());
        course.setAdditionalInfo(request.getAdditionalInfo());

        repository.save(course);

        producer.produceCoursesUpdate(newScheduleId);

        return mapper.map(course);
    }

    @Transactional
    public void delete(UUID id) {
        Course course = findByIdOrElseThrow(id);

        producer.produceCoursesUpdate(course.getScheduleId());

        repository.delete(course);
    }

    public List<CourseResponse> findCoursesBySchedule(UUID scheduleId) {
        return repository.findByScheduleId(scheduleId).stream()
                .sorted(courseComparator)
                .map(mapper::map)
                .toList();
    }

    @Transactional
    public void handleScheduleDelete(UUID scheduleId) {
        repository.deleteAll(repository.findByScheduleId(scheduleId));
    }

    @Transactional
    public void handleMappedCourses(UUID scheduleId, Set<Course> retrievedCourses) {
        List<Course> currentCourses = repository.findByScheduleId(scheduleId);

        Set<Course> coursesToSave = retrievedCourses.stream()
                .filter(retrievedCourse -> currentCourses.stream()
                        .noneMatch(currentCourse -> haveSameParameters(
                                retrievedCourse,
                                currentCourse)
                        ))
                .collect(Collectors.toSet());

        Set<Course> coursesToDelete = currentCourses.stream()
                .filter(currentCourse -> retrievedCourses.stream()
                        .noneMatch(retrievedCourse -> haveSameParameters(
                                retrievedCourse,
                                currentCourse)
                        ))
                .collect(Collectors.toSet());

        repository.deleteAll(coursesToDelete);
        repository.saveAll(coursesToSave);

        if (!coursesToDelete.isEmpty() || !coursesToSave.isEmpty()) {
            producer.produceCoursesUpdate(scheduleId);
        }
    }

    private boolean haveSameParameters(Course course1, Course course2) {
        return Objects.equals(course1.getName(), course2.getName()) &&
                course1.getCourseType() == course2.getCourseType() &&
                Objects.equals(course1.getTeachers(), course2.getTeachers()) &&
                Objects.equals(course1.getClassroom(), course2.getClassroom()) &&
                Objects.equals(course1.getAdditionalInfo(), course2.getAdditionalInfo()) &&
                course1.getDayOfWeek() == course2.getDayOfWeek() &&
                course1.getWeekType() == course2.getWeekType() &&
                Objects.equals(course1.getStartsAt(), course2.getStartsAt()) &&
                Objects.equals(course1.getEndsAt(), course2.getEndsAt());
    }
}
