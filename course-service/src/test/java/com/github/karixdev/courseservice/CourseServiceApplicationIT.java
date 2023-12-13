package com.github.karixdev.courseservice;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseServiceApplicationIT extends ContainersEnvironment {

    @Autowired
    CourseRepository courseRepository;

    KafkaTemplate<String, ScheduleDomain> scheduleDomainKafkaTemplate;
    Consumer<String, ScheduleCoursesEvent> scheduleCoursesEventConsumer;

    private static final String SCHEDULE_DOMAIN_TOPIC = "schedule.domain";
    private static final String SCHEDULE_COURSES_EVENT_TOPIC = "schedule.courses.event";

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, ScheduleDomain> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        scheduleDomainKafkaTemplate = new KafkaTemplate<>(producerFactory);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafkaContainer.getBootstrapServers(),
                "course-service-schedule-courses-event-test",
                "true"
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, ScheduleCoursesEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        scheduleCoursesEventConsumer = consumerFactory.createConsumer();
        scheduleCoursesEventConsumer.subscribe(List.of(SCHEDULE_COURSES_EVENT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
        scheduleCoursesEventConsumer.close();
    }

    @Test
    void shouldUpdateScheduleCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Course course1 = Course.builder()
                .name("Physics")
                .scheduleId(scheduleId)
                .courseType(CourseType.LAB)
                .teachers("dr. Max")
                .classroom("408MS")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course2 = Course.builder()
                .name("C++")
                .scheduleId(scheduleId)
                .courseType(CourseType.LECTURE)
                .teachers("dr. Henryk")
                .classroom("CEK Room C")
                .additionalInfo("contact teacher")
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        courseRepository.saveAll(List.of(course1, course2));

        CourseDomain courseDomain1 = CourseDomain.builder()
                .name("Calculus I")
                .courseType(com.github.karixdev.commonservice.model.course.domain.CourseType.PRACTICAL)
                .teachers("dr. Adam")
                .classrooms("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weeks(com.github.karixdev.commonservice.model.course.domain.WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        CourseDomain courseDomain2 = CourseDomain.builder()
                .name("Physics")
                .courseType(com.github.karixdev.commonservice.model.course.domain.CourseType.LAB)
                .teachers("dr. Max")
                .classrooms("408MS")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weeks(com.github.karixdev.commonservice.model.course.domain.WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        ScheduleDomain scheduleDomain = ScheduleDomain.builder()
                .scheduleId(scheduleId.toString())
                .courses(Set.of(courseDomain1, courseDomain2))
                .build();

        // When
        scheduleDomainKafkaTemplate.send(SCHEDULE_DOMAIN_TOPIC, scheduleId.toString(), scheduleDomain);

        // Then
        ConsumerRecord<String, ScheduleCoursesEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleCoursesEventConsumer, SCHEDULE_COURSES_EVENT_TOPIC, Duration.ofSeconds(20));

        List<Course> courses = courseRepository.findAll();

        assertThat(courses)
                .hasSize(2)
                .doesNotContain(course2)
                .anySatisfy(course -> assertThat(course).isEqualTo(course1))
                .anySatisfy(course -> {
                    assertThat(course.getName()).isEqualTo(courseDomain1.name());
                    assertThat(course.getCourseType()).isEqualTo(CourseType.PRACTICAL);
                    assertThat(course.getTeachers()).isEqualTo(courseDomain1.teachers());
                    assertThat(course.getClassroom()).isEqualTo(courseDomain1.classrooms());
                    assertThat(course.getDayOfWeek()).isEqualTo(courseDomain1.dayOfWeek());
                    assertThat(course.getWeekType()).isEqualTo(WeekType.ODD);
                    assertThat(course.getStartsAt()).isEqualTo(courseDomain1.startsAt());
                    assertThat(course.getEndsAt()).isEqualTo(courseDomain1.endsAt());
                });

        Course newCourse = courses.stream().filter(course -> !course.equals(course1)).toList().get(0);

        assertThat(consumerRecord.value().deleted()).containsExactly(course2.getId().toString());
        assertThat(consumerRecord.value().created()).containsExactly(newCourse.getId().toString());
    }

}