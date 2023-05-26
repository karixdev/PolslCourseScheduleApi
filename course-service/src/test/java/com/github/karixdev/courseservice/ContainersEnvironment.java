package com.github.karixdev.courseservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public abstract class ContainersEnvironment {
    static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15.1-alpine")
                    .withUsername("root")
                    .withPassword("root")
                    .withDatabaseName("schedule-service-test")
                    .withReuse(true);

    static final RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.11.7-management-alpine")
                    .withUser("user", "password")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        rabbitMQContainer.start();
    }

    @DynamicPropertySource
    static void overrideDatabaseConnectionProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgreSQLContainer::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                postgreSQLContainer::getUsername);

        registry.add(
                "spring.datasource.password",
                postgreSQLContainer::getPassword);

        registry.add(
                "spring.datasource.driver-class-name",
                postgreSQLContainer::getDriverClassName);
    }

    @DynamicPropertySource
    static void overrideRabbitMqProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.rabbitmq.host",
                rabbitMQContainer::getHost);

        registry.add(
                "spring.rabbitmq.port",
                rabbitMQContainer::getAmqpPort);

        registry.add(
                "spring.rabbitmq.password",
                rabbitMQContainer::getAdminPassword);

        registry.add(
                "spring.rabbitmq.username",
                rabbitMQContainer::getAdminUsername);
    }
}
