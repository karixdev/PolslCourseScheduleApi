package com.github.karixdev.courseservice;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public class RestControllerITContainersEnvironment {

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

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        keycloakContainer.start();
    }

    @DynamicPropertySource
    static void overrideDatabaseConnectionProperties(DynamicPropertyRegistry registry) {
        if (!postgreSQLContainer.isCreated()) {
            return;
        }

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
        if (!keycloakContainer.isCreated()) {
            return;
        }

        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/polsl-course-api");
    }

}
