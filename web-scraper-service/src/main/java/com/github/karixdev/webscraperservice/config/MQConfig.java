package com.github.karixdev.webscraperservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.*;
import static com.github.karixdev.webscraperservice.props.ScheduleEventMQProperties.*;

@Configuration
public class MQConfig {
    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    TopicExchange scheduleEventExchange() {
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
                .to(scheduleEventExchange())
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
                .to(scheduleEventExchange())
                .with(SCHEDULE_UPDATE_ROUTING_KEY);
    }

    @Bean
    TopicExchange coursesExchange() {
        return new TopicExchange(COURSES_EXCHANGE);
    }

    @Bean
    Queue rawCoursesQueue() {
        return new Queue(RAW_COURSES_QUEUE);
    }

    @Bean
    Binding rawCoursesBinding() {
        return BindingBuilder
                .bind(rawCoursesQueue())
                .to(coursesExchange())
                .with(RAW_COURSES_ROUTING_KEY);
    }
}
