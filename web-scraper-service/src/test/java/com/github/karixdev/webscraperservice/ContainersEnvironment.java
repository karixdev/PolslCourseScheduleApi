package com.github.karixdev.webscraperservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public class ContainersEnvironment {
    static final RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.11.7-management-alpine")
                    .withUser("user", "password")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        rabbitMQContainer.start();
    }

    @DynamicPropertySource
    static void overrideRabbitMQConnectionProps(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.rabbitmq.host",
                rabbitMQContainer::getHost
        );

        registry.add(
                "spring.rabbitmq.port",
                rabbitMQContainer::getAmqpPort
        );

        registry.add(
                "spring.rabbitmq.password",
                rabbitMQContainer::getAdminPassword
        );

        registry.add(
                "spring.rabbitmq.username",
                rabbitMQContainer::getAdminUsername
        );
    }
}
