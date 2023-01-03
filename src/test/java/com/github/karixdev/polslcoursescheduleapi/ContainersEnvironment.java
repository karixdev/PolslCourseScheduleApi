package com.github.karixdev.polslcoursescheduleapi;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@Testcontainers
@ActiveProfiles("test")
public class ContainersEnvironment {
    static final MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:latest")
                    .withDatabaseName("rating-youtube-thumbnails-test")
                    .withUsername("root")
                    .withPassword("root")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        mySQLContainer.start();
    }

    @DynamicPropertySource
    static void overrideDatabaseConnectionProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add(
                "spring.datasource.url",
                mySQLContainer::getJdbcUrl);

        dynamicPropertyRegistry.add(
                "spring.datasource.username",
                mySQLContainer::getUsername);

        dynamicPropertyRegistry.add(
                "spring.datasource.password",
                mySQLContainer::getPassword);

        dynamicPropertyRegistry.add(
                "spring.datasource.driver-class-name",
                mySQLContainer::getDriverClassName);
    }
}
