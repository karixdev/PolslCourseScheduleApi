package com.github.karixdev.courseservice.testconfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@TestConfiguration
public class TestKafkaTopicsConfig {

    @Bean
    NewTopic processedRawScheduleTopic(@Value("${kafka.topics.processed-raw-schedule}") String topic) {
        return TopicBuilder.name(topic).build();
    }

    @Bean
    NewTopic scheduleEventTopic(@Value("${kafka.topics.schedule-event}") String topic) {
        return TopicBuilder.name(topic).build();
    }

}
