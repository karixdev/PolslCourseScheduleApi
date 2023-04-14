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

    static final KeycloakContainer keycloakContainer =
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

        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/polsl-course-api");
    }

    private record KeyCloakToken(String accessToken) {
        @JsonCreator
        private KeyCloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }

    private String getToken(String username, String password) {
        WebClient webClient = WebClient.builder().build();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("grant_type", Collections.singletonList("password"));
        map.put("client_id", Collections.singletonList("test-client"));
        map.put("client_secret", Collections.singletonList("yRKGZsSpZ7I2msjSLxadwJF3qePvQI1V"));
        map.put("username", Collections.singletonList(username));
        map.put("password", Collections.singletonList(password));

        KeyCloakToken token = webClient.post().uri(keycloakContainer.getAuthServerUrl() + "/realms/polsl-course-api/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(map)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KeyCloakToken.class)
                .block();

        assert token != null;
        return token.accessToken();
    }

    protected String getAdminToken() {
        return getToken("admin", "admin");
    }

    protected String getUserToken() {
        return getToken("user", "user");
    }
}
