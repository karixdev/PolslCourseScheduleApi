package com.github.karixdev.scheduleservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

@Testcontainers
@ActiveProfiles("test")
public abstract class ContainersEnvironment {
    protected static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15.1-alpine")
                    .withUsername("root")
                    .withPassword("root")
                    .withDatabaseName("schedule-service-test")
                    .withReuse(true);

    protected static final RabbitMQContainer rabbitMQContainer =
            new RabbitMQContainer("rabbitmq:3.11.7-management-alpine")
                    .withUser("user", "password")
                    .withReuse(true);

    protected static final KeycloakContainer keycloakContainer =
            new KeycloakContainer()
                    .withAdminUsername("admin")
                    .withAdminPassword("admin")
                    .withRealmImportFile("keycloak/realm.json")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        rabbitMQContainer.start();
        keycloakContainer.start();
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
    static void overrideRabbitMQProperties(DynamicPropertyRegistry registry) {
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

    @DynamicPropertySource
    static void overrideAuthServerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/polsl-course-api");
    }

}
