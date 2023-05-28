package com.github.karixdev.domaincoursemapperservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.*;

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
    TopicExchange coursesExchange() {
        return new TopicExchange(COURSES_EXCHANGE);
    }

    @Bean
    Queue domainCoursesQueue() {
        return new Queue(DOMAIN_COURSES_QUEUE);
    }

    @Bean
    Binding domainCoursesBinding() {
        return BindingBuilder
                .bind(domainCoursesQueue())
                .to(coursesExchange())
                .with(DOMAIN_COURSES_ROUTING_KEY);
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
