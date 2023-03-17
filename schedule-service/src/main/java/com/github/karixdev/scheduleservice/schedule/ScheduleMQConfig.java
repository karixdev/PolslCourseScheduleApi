package com.github.karixdev.scheduleservice.schedule;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.*;

@Configuration
public class ScheduleMQConfig {
    @Bean
    TopicExchange topic() {
        return new TopicExchange(SCHEDULE_TOPIC);
    }

    @Bean
    Queue scheduleUpdateRequestQueue() {
        return new Queue(SCHEDULE_UPDATE_REQUEST_QUEUE);
    }

    @Bean
    Binding scheduleUpdateRequestBinding() {
        return BindingBuilder
                .bind(scheduleUpdateRequestQueue())
                .to(topic())
                .with(SCHEDULE_UPDATE_REQUEST_ROUTING_KEY);
    }

    @Bean
    Queue scheduleUpdateResponseQueue() {
        return new Queue(SCHEDULE_UPDATE_RESPONSE_QUEUE);
    }

    @Bean
    Binding scheduleUpdateResponseBinding() {
        return BindingBuilder
                .bind(scheduleUpdateResponseQueue())
                .to(topic())
                .with(SCHEDULE_UPDATE_RESPONSE_ROUTING_KEY);
    }
}