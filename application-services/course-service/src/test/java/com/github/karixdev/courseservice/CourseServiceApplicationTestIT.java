package com.github.karixdev.courseservice;

import com.github.karixdev.courseservice.application.event.EventType;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.domain.entity.schedule.Schedule;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import com.github.karixdev.courseservice.infrastructure.dal.repository.CourseEntityRepository;
import com.github.karixdev.courseservice.testconfig.TestKafkaTopicsConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = TestKafkaTopicsConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseServiceApplicationTestIT extends ContainersEnvironment {

    @Autowired
    CourseEntityRepository courseRepository;

    KafkaTemplate<String, ScheduleEvent> scheduleEventKafkaTemplate;

    static final String SCHEDULE_EVENT_TOPIC = "schedule.event";

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, ScheduleEvent> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        scheduleEventKafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void shouldDeleteCoursesWithScheduleId() {
        UUID scheduleId = UUID.randomUUID();
        List<CourseEntity> courses = IntStream.range(0, 5)
                .mapToObj(i -> CourseEntity.builder()
                        .scheduleId(scheduleId)
                        .name("course-" + i)
                        .courseType(CourseEntityCourseType.LAB)
                        .teachers("teacher-" + i)
                        .classrooms("classroom-" + i)
                        .additionalInfo("additional-info-" + i)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .weekType(CourseEntityWeekType.EVEN)
                        .startsAt(LocalTime.of(i, 10))
                        .endsAt(LocalTime.of(10, i))
                        .build())
                .toList();

        List<CourseEntity> otherCourses = IntStream.range(0, 5)
                .mapToObj(i -> CourseEntity.builder()
                        .scheduleId(UUID.randomUUID())
                        .name("course-2-" + i)
                        .teachers("teacher-2-" + i)
                        .courseType(CourseEntityCourseType.INFO)
                        .classrooms("classroom-2-" + i)
                        .additionalInfo("additional-info-2-" + i)
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .weekType(CourseEntityWeekType.ODD)
                        .startsAt(LocalTime.of(i, 12))
                        .endsAt(LocalTime.of(13, i))
                        .build())
                .toList();

        courseRepository.saveAll(Stream.concat(courses.stream(), otherCourses.stream()).toList());

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .type(EventType.DELETE)
                .entity(
                        Schedule.builder()
                                .id(scheduleId)
                                .build()
                )
                .build();

        scheduleEventKafkaTemplate.send(SCHEDULE_EVENT_TOPIC, scheduleId.toString(), scheduleEvent);

        await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(courseRepository.findAll()).isEqualTo(otherCourses));
    }

    void addCommonConsumerProps(Map<String, Object> consumerProps) {
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    }

}