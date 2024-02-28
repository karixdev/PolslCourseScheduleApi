package com.github.karixdev.scheduleservice.infrastructure.scheduled;

import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.application.service.ScheduleService;
import com.github.karixdev.scheduleservice.infrastructure.dal.JpaScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleJobIT extends ContainersEnvironment {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    JpaScheduleRepository scheduleRepository;

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

        ConsumerFactory<String, ScheduleEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        scheduleEventConsumer = consumerFactory.createConsumer();
        scheduleEventConsumer.subscribe(List.of(SCHEDULE_EVENT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
        scheduleEventConsumer.close();
    }

    @Test
    void shouldRequestForThreeUpdates() {
        scheduleRepository.saveAll(List.of(
                ScheduleEntity.builder()
                        .type(1)
                        .planPolslId(1)
                        .semester(1)
                        .name("schedule-name-1")
                        .groupNumber(1)
                        .wd(0)
                        .build(),
                ScheduleEntity.builder()
                        .type(1)
                        .planPolslId(2)
                        .semester(1)
                        .name("schedule-name-2")
                        .groupNumber(1)
                        .wd(0)
                        .build(),
                ScheduleEntity.builder()
                        .type(1)
                        .planPolslId(3)
                        .semester(1)
                        .name("schedule-name-3")
                        .groupNumber(1)
                        .wd(0)
                        .build()
        ));

        ConsumerRecords<String, ScheduleEvent> consumerRecords = KafkaTestUtils.getRecords(scheduleEventConsumer, Duration.ofSeconds(20));
        assertThat(consumerRecords.count()).isEqualTo(3);
    }

}
