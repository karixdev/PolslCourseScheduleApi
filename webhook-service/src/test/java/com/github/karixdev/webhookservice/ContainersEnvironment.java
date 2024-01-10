package com.github.karixdev.webhookservice;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
public abstract class ContainersEnvironment {

	protected static final MongoDBContainer mongoDBContainer =
			new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"))
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
		mongoDBContainer.start();
		keycloakContainer.start();
		kafkaContainer.start();
	}

	@DynamicPropertySource
	static void overrideDBConnectionProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
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

	@DynamicPropertySource
	static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
		if (!kafkaContainer.isCreated()) {
			return;
		}

		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
	}

}
