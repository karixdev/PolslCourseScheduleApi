package com.github.karixdev.discordnotificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.discordnotificationservice.props.CourseEventMQProperties.*;
import static com.github.karixdev.discordnotificationservice.props.NotificationMQProperties.*;

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

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE);
    }

    @Bean
    Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }
}
