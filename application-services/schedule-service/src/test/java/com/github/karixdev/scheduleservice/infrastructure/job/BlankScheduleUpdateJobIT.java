package com.github.karixdev.scheduleservice.infrastructure.job;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import com.github.karixdev.scheduleservice.infrastructure.dal.repository.JpaScheduleRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BlankScheduleUpdateJobIT extends ContainersEnvironment {

    @Autowired
    JpaScheduleRepository jpaScheduleRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    Consumer<String, ScheduleEvent> scheduleEventConsumer;
    private static final String SCHEDULE_EVENT_TOPIC = "schedule.event";

    @DynamicPropertySource
    static void overrideScheduleJobCron(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule.job.cron",
                () -> "*/5 * * * * *");
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "schedule-domain-test-consumer", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, ScheduleEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(ScheduleEvent.class, false)
        );
        scheduleEventConsumer = consumerFactory.createConsumer();
        scheduleEventConsumer.subscribe(List.of(SCHEDULE_EVENT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        jpaScheduleRepository.deleteAll();
        scheduleEventConsumer.close();
    }

    @Test
    void shouldProduceEvents() {
        List<ScheduleEntity> schedules = IntStream.range(0, 15).mapToObj(i ->
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .type(i + 1)
                        .planPolslId(i + 1)
                        .semester(i + 1)
                        .major("major-" + i)
                        .groupNumber(i + 1)
                        .wd(i + 1)
                        .build()
        ).toList();

        jpaScheduleRepository.saveAll(schedules);

        List<String> ids = schedules.stream()
                .map(entity -> entity.getId().toString())
                .toList();

        ConsumerRecords<String, ScheduleEvent> events = KafkaTestUtils.getRecords(scheduleEventConsumer, Duration.ofSeconds(20), 15);
        Iterator<ConsumerRecord<String, ScheduleEvent>> iterator = events.iterator();

        List<String> eventsSchedulesIds = new ArrayList<>();
        while (iterator.hasNext()) {
            eventsSchedulesIds.add(iterator.next().value().scheduleId());
        }

        assertThat(eventsSchedulesIds).isEqualTo(ids);
    }

}