package com.github.karixdev.courseservice;

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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

@Testcontainers
@ActiveProfiles("test")
public abstract class ContainersEnvironment {

    protected static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15.1-alpine")
                    .withUsername("root")
                    .withPassword("root")
                    .withDatabaseName("course-service-test")
                    .withReuse(true);

    protected static final KeycloakContainer keycloakContainer =
            new KeycloakContainer()
                    .withAdminUsername("admin")
                    .withAdminPassword("admin")
                    .withRealmImportFile("keycloak/realm.json")
                    .withReuse(true);

    protected static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.3"));

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        keycloakContainer.start();
        kafkaContainer.start();
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
    static void overrideSecurityProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/polsl-course-api");
    }

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
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
