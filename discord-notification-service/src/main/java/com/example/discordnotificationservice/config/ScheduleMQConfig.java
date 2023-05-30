package com.example.discordnotificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.discordnotificationservice.props.ScheduleMQProperties.*;

@Configuration
public class ScheduleMQConfig {
    @Bean
    TopicExchange topic() {
        return new TopicExchange(SCHEDULE_TOPIC);
    }

    @Bean
    Queue scheduleUpdateResponseQueue() {
        return new Queue(SCHEDULE_UPDATE_QUEUE);
    }

    @Bean
    Binding scheduleUpdateBinding() {
        return BindingBuilder
                .bind(scheduleUpdateResponseQueue())
                .to(topic())
                .with(SCHEDULE_UPDATE_ROUTING_KEY);
    }
}
