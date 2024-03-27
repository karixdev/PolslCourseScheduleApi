package com.github.karixdev.domainmodelmapperservice;

import com.github.karixdev.domainmodelmapperservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.RawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.domain.processed.CourseType;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawSchedule;
import com.github.karixdev.domainmodelmapperservice.domain.processed.WeekType;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawSchedule;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawTimeInterval;
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

    private static final String RAW_SCHEDULE_TOPIC = "schedule.raw";
    private static final String PROCESSED_RAW_SCHEDULE_TOPIC = "schedule.raw-processed";
    private static final String DLT_TOPIC = "domain-model-mapper-service.schedule.raw.dlt";

    KafkaTemplate<String, RawScheduleEvent> rawScheduleEventProducer;
    Consumer<String, ProcessedRawScheduleEvent> processedRawScheduleEventConsumer;
    Consumer<String, RawScheduleEvent> dltConsumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, RawScheduleEvent> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        rawScheduleEventProducer = new KafkaTemplate<>(producerFactory);

        Map<String, Object> processedRawScheduleEventProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "schedule-domain-test-group", "false");
        addCommonConsumerProps(processedRawScheduleEventProps);

        ConsumerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventConsumerFactory = new DefaultKafkaConsumerFactory<>(processedRawScheduleEventProps);
        processedRawScheduleEventConsumer = processedRawScheduleEventConsumerFactory.createConsumer();
        processedRawScheduleEventConsumer.subscribe(List.of(PROCESSED_RAW_SCHEDULE_TOPIC));

        Map<String, Object> dltConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "dlt-test-group", "false");
        addCommonConsumerProps(dltConsumerProps);

        ConsumerFactory<String, RawScheduleEvent> rawScheduleEventConsumerFactory = new DefaultKafkaConsumerFactory<>(dltConsumerProps);
        dltConsumer = rawScheduleEventConsumerFactory.createConsumer();
        dltConsumer.subscribe(List.of(DLT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        processedRawScheduleEventConsumer.close();
        dltConsumer.close();
    }

    @Test
    void shouldConsumeScheduleRawAndProduceMappedScheduleDomainToDomainTopic() {
        // Given

        RawTimeInterval rawTimeInterval = new RawTimeInterval("08:30", "10:00");
        RawCourse rawCourse = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .text("course 1")
                .build();

        String scheduleId = UUID.randomUUID().toString();
        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of(rawCourse))
                .timeIntervals(Set.of(rawTimeInterval))
                .build();

        RawScheduleEvent rawScheduleEvent = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        ProcessedRawCourse course = ProcessedRawCourse.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(11, 45))
                .name("course 1")
                .courseType(CourseType.INFO)
                .teachers("")
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .classroom("")
                .build();

        ProcessedRawSchedule processedRawSchedule = ProcessedRawSchedule.builder()
                .courses(Set.of(course))
                .build();

        ProcessedRawScheduleEvent expectedEvent = ProcessedRawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(processedRawSchedule)
                .build();

        // When
        rawScheduleEventProducer.send(RAW_SCHEDULE_TOPIC, scheduleId, rawScheduleEvent);
        ConsumerRecord<String, ProcessedRawScheduleEvent> result = KafkaTestUtils.getSingleRecord(processedRawScheduleEventConsumer, PROCESSED_RAW_SCHEDULE_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldProduceProblematicEventOnDLT() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of())
                .timeIntervals(Set.of())
                .build();
        RawScheduleEvent rawScheduleEvent = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        // When
        rawScheduleEventProducer.send(RAW_SCHEDULE_TOPIC, scheduleId, rawScheduleEvent);
        ConsumerRecord<String, RawScheduleEvent> result = KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(rawScheduleEvent);
    }

    private void addCommonConsumerProps(Map<String, Object> consumerProps) {
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    }

}