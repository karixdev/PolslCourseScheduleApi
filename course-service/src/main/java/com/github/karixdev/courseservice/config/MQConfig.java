package com.github.karixdev.courseservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.courseservice.props.ScheduleMQProperties.*;
import static com.github.karixdev.courseservice.props.MappedCoursesMQProperties.*;
import static com.github.karixdev.courseservice.props.CourseEventMQProperties.*;

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
    TopicExchange scheduleEventsTopic() {
        return new TopicExchange(SCHEDULE_TOPIC);
    }

    @Bean
    Queue scheduleUpdateQueue() {
        return new Queue(SCHEDULE_UPDATE_QUEUE);
    }

    @Bean
    Binding scheduleUpdateRequestBinding() {
        return BindingBuilder
                .bind(scheduleUpdateQueue())
                .to(scheduleEventsTopic())
                .with(SCHEDULE_UPDATE_ROUTING_KEY);
    }

    @Bean
    TopicExchange domainCourseExchange() {
        return new TopicExchange(DOMAIN_COURSE_EXCHANGE);
    }

    @Bean
    Queue domainCourseQueue() {
        return new Queue(DOMAIN_COURSE_QUEUE);
    }

    @Bean
    Binding domainCourseBinding() {
        return BindingBuilder
                .bind(domainCourseQueue())
                .to(domainCourseExchange())
                .with(DOMAIN_COURSE_ROUTING_KEY);
    }

    @Bean
    TopicExchange coursesUpdateExchange() {
        return new TopicExchange(COURSES_UPDATE_EXCHANGE);
    }

    @Bean
    Queue coursesUpdateQueue() {
        return new Queue(COURSES_UPDATE_QUEUE);
    }

    @Bean
    Binding coursesUpdateBinding() {
        return BindingBuilder
                .bind(coursesUpdateQueue())
                .to(coursesUpdateExchange())
                .with(COURSES_UPDATE_ROUTING_KEY);
    }
}