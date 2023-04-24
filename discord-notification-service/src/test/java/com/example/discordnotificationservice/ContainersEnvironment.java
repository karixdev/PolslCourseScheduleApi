package com.example.discordnotificationservice;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public abstract class ContainersEnvironment {
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:4.4.2")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void overrideDatabaseConnectionProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl);
    }
}
