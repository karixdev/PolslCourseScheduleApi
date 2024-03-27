package com.github.karixdev.domainmodelmapperservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTestTopologyConfig {

    @Bean
    NewTopic scheduleRawTopic(@Value("${kafka.topics.schedule-raw}") String name) {
        return TopicBuilder.name(name).build();
    }

    @Bean
    NewTopic scheduleDomainTopic(@Value("${kafka.topics.schedule-domain}") String name) {
        return TopicBuilder.name(name).build();
    }

    @Bean
    NewTopic dlt(@Value("${kafka.topics.dlt}") String name) {
        return TopicBuilder.name(name).build();
    }

}
