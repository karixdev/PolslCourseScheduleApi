package com.github.karixdev.courseservice.service;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.commonservice.exception.ValidationException;
import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.comparator.CourseComparator;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.producer.ScheduleCoursesEventProducer;
import com.github.karixdev.courseservice.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final ScheduleClient scheduleClient;
    private final CourseComparator courseComparator;
    private final ScheduleCoursesEventProducer producer;

    @Transactional
    public CourseResponse create(CourseRequest request) {
        UUID scheduleId = request.getScheduleId();
        if (!doesScheduleExist(scheduleId)) {
            throw new ValidationException(
                    "scheduleId",
                    "Schedule with id %s does not exist".formatted(scheduleId)
            );
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
        producer.produceCreated(scheduleId, Set.of(course));

        return mapper.map(course);
    }

    private boolean doesScheduleExist(UUID scheduleId) {
        try {
            return scheduleClient.findById(scheduleId).isPresent();
        } catch (HttpServiceClientException e) {
            log.error("Schedule service returned client error status", e);
            return false;
        }
    }

    private Course findByIdOrElseThrow(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Course with it %s not found".formatted(id)));
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        Course course = findByIdOrElseThrow(id);

        UUID newScheduleId = request.getScheduleId();
        UUID oldScheduleId = course.getScheduleId();

        if (!oldScheduleId.equals(request.getScheduleId()) && !doesScheduleExist(newScheduleId)) {
            throw new ValidationException(
                    "scheduleId",
                    "Schedule with id %s does not exist".formatted(newScheduleId)
            );
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
        producer.produceUpdated(newScheduleId, Set.of(course));

        return mapper.map(course);
    }

    @Transactional
    public void delete(UUID id) {
        Course course = findByIdOrElseThrow(id);

        repository.delete(course);
        producer.produceDeleted(course.getScheduleId(), Set.of(course));
    }

    public List<CourseResponse> findCoursesBySchedule(UUID scheduleId) {
        return repository.findByScheduleId(scheduleId).stream()
                .sorted(courseComparator)
                .map(mapper::map)
                .toList();
    }

    @Transactional
    public void updateScheduleCourses(UUID scheduleId, Set<Course> retrievedCourses) {
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
            producer.produceCreatedAndDeleted(scheduleId, coursesToSave, coursesToDelete);
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

    public void handleScheduleEvent(ScheduleEvent value) {
        if (value.eventType() != EventType.DELETE) {
            return;
        }

        UUID scheduleId = UUID.fromString(value.scheduleId());
        repository.deleteAll(repository.findByScheduleId(scheduleId));
    }
}
