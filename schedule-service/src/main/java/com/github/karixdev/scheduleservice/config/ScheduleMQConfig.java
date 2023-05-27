package com.github.karixdev.scheduleservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.*;

@Configuration
public class ScheduleMQConfig {
    @Bean
    TopicExchange scheduleEventsExchange() {
        return new TopicExchange(SCHEDULE_EXCHANGE);
    }

    @Bean
    Queue scheduleCreateQueue() {
        return new Queue(SCHEDULE_CREATE_QUEUE);
    }

    @Bean
    Binding scheduleCreateBinding() {
        return BindingBuilder
                .bind(scheduleCreateQueue())
                .to(scheduleEventsExchange())
                .with(SCHEDULE_CREATE_ROUTING_KEY);
    }

    @Bean
    Queue scheduleUpdateQueue() {
        return new Queue(SCHEDULE_UPDATE_QUEUE);
    }

    @Bean
    Binding scheduleUpdateBinding() {
        return BindingBuilder
                .bind(scheduleUpdateQueue())
                .to(scheduleEventsExchange())
                .with(SCHEDULE_UPDATE_ROUTING_KEY);
    }

    @Bean
    Queue scheduleDeleteQueue() {
        return new Queue(SCHEDULE_DELETE_QUEUE);
    }

    @Bean
    Binding scheduleDeleteBinding() {
        return BindingBuilder
                .bind(scheduleDeleteQueue())
                .to(scheduleEventsExchange())
                .with(SCHEDULE_DELETE_ROUTING_KEY);
    }
}
