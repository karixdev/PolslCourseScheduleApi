package com.github.karixdev.domainmodelmapperservice;

import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseType;
import com.github.karixdev.commonservice.model.course.domain.WeekType;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DomainModelMapperServiceApplicationIntegrationTest {

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.3"));

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    private static final String SCHEDULE_RAW_TOPIC = "schedule.raw";
    private static final String SCHEDULE_DOMAIN_TOPIC = "schedule.domain";
    private static final String DLT_TOPIC = "domain-model-mapper-service.schedule.raw.dlt";

    KafkaTemplate<String, ScheduleRaw> scheduleRawProducer;
    Consumer<String, ScheduleDomain> scheduleDomainConsumer;
    Consumer<String, ScheduleRaw> dltConsumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, ScheduleRaw> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        scheduleRawProducer = new KafkaTemplate<>(producerFactory);

        Map<String, Object> scheduleDomainConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "schedule-domain-test-group", "false");
        addCommonConsumerProps(scheduleDomainConsumerProps);

        ConsumerFactory<String, ScheduleDomain> scheduleDomainConsumerFactory = new DefaultKafkaConsumerFactory<>(scheduleDomainConsumerProps);
        scheduleDomainConsumer = scheduleDomainConsumerFactory.createConsumer();
        scheduleDomainConsumer.subscribe(List.of(SCHEDULE_DOMAIN_TOPIC));

        Map<String, Object> dltConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "dlt-test-group", "false");
        addCommonConsumerProps(dltConsumerProps);

        ConsumerFactory<String, ScheduleRaw> rawCourseConsumerFactory = new DefaultKafkaConsumerFactory<>(dltConsumerProps);
        dltConsumer = rawCourseConsumerFactory.createConsumer();
        dltConsumer.subscribe(List.of(DLT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        scheduleDomainConsumer.close();
        dltConsumer.close();
    }

    @Test
    void shouldConsumeScheduleRawAndProduceMappedScheduleDomainToDomainTopic() {
        // Given
        CourseCell courseCell = CourseCell.builder()
                .top(259)
                .left(254)
                .ch(135)
                .cw(154)
                .text("course 1")
                .build();
        TimeCell timeCell = new TimeCell("08:30-10:00");

        String scheduleId = UUID.randomUUID().toString();
        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(scheduleId)
                .timeCells(Set.of(timeCell))
                .courseCells(Set.of(courseCell))
                .build();

        CourseDomain courseDomain = CourseDomain.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(11, 45))
                .name("course 1")
                .courseType(CourseType.INFO)
                .teachers("")
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weeks(WeekType.EVERY)
                .classrooms("")
                .build();
        ScheduleDomain scheduleDomain = ScheduleDomain.builder()
                .scheduleId(scheduleId)
                .courses(Set.of(courseDomain))
                .build();

        // When
        scheduleRawProducer.send(SCHEDULE_RAW_TOPIC, scheduleId, scheduleRaw);
        ConsumerRecord<String, ScheduleDomain> result = KafkaTestUtils.getSingleRecord(scheduleDomainConsumer, SCHEDULE_DOMAIN_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(scheduleDomain);
    }

    @Test
    void shouldProduceProblematicEventOnDLT() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        ScheduleRaw scheduleRaw = ScheduleRaw.builder()
                .scheduleId(scheduleId)
                .timeCells(Set.of())
                .courseCells(Set.of())
                .build();


        // When
        scheduleRawProducer.send(SCHEDULE_RAW_TOPIC, scheduleId, scheduleRaw);
        ConsumerRecord<String, ScheduleRaw> result = KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(scheduleRaw);
    }

    private void addCommonConsumerProps(Map<String, Object> consumerProps) {
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    }

}