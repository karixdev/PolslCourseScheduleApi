package com.github.karixdev.webscraperservice;

import com.github.karixdev.webscraperservice.application.event.EventType;
import com.github.karixdev.webscraperservice.application.event.RawScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.ScheduleEvent;
import com.github.karixdev.webscraperservice.application.props.PlanPolslClientProperties;
import com.github.karixdev.webscraperservice.domain.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class WebScraperServiceApplicationIntegrationIntegrationTest {

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.3"));

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @DynamicPropertySource
    static void overridePlanPolslUrl(DynamicPropertyRegistry registry) {
        registry.add("plan-polsl-url", () -> "http://localhost:9999/");
    }

    private static final String RAW_TOPIC = "schedule.raw";
    private static final String SCHEDULE_EVENT_TOPIC = "schedule.event";
    private static final String DLT_TOPIC = "web-scraper-service.schedule.event.dlt";

    KafkaTemplate<String, ScheduleEvent> scheduleEventProducer;
    Consumer<String, RawScheduleEvent> rawScheduleConsumer;
    Consumer<String, ScheduleEvent> dltConsumer;

    WireMockServer wm;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, ScheduleEvent> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        scheduleEventProducer = new KafkaTemplate<>(producerFactory);

        Map<String, Object> rawScheduleConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "raw-schedule-test-group", "false");
        addCommonConsumerProps(rawScheduleConsumerProps);

        ConsumerFactory<String, RawScheduleEvent> rawCourseConsumerFactory = new DefaultKafkaConsumerFactory<>(rawScheduleConsumerProps);
        rawScheduleConsumer = rawCourseConsumerFactory.createConsumer();
        rawScheduleConsumer.subscribe(List.of(RAW_TOPIC));

        Map<String, Object> dltConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "dlt-test-group", "false");
        addCommonConsumerProps(dltConsumerProps);

        ConsumerFactory<String, ScheduleEvent> scheduleEventConsumerFactory = new DefaultKafkaConsumerFactory<>(dltConsumerProps);
        dltConsumer = scheduleEventConsumerFactory.createConsumer();
        dltConsumer.subscribe(List.of(DLT_TOPIC));

        wm = new WireMockServer(9999);
        wm.start();
    }

    @AfterEach
    void tearDown() {
        rawScheduleConsumer.close();
        dltConsumer.close();
        wm.stop();
    }

    @Test
    void shouldConsumeScheduleEventAndThenProduceRawCourseToTopic() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        int planPolslId = 1337;
        int type = 0;
        int wd = 4;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .planPolslId(planPolslId)
                .type(type)
                .wd(wd)
                .build();

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(schedule)
                .type(EventType.CREATE)
                .build();

        RawTimeInterval timeInterval = RawTimeInterval.builder()
                .start("07:00")
                .end("08:00")
                .build();

        Set<RawAnchor> anchors = Set.of(
                RawAnchor.builder()
                        .address("https://site.com")
                        .text("Site")
                        .build(),
                RawAnchor.builder()
                        .address("https://other-site.com")
                        .text("Other site")
                        .build()
        );

        RawCourse course = RawCourse.builder()
                .top(30)
                .left(40)
                .height(10)
                .width(20)
                .text("This is course div")
                .anchors(anchors)
                .build();

        RawSchedule rawSchedule = RawSchedule.builder()
                .timeIntervals(Set.of(timeInterval))
                .courses(Set.of(course))
                .build();

        RawScheduleEvent expectedEvent = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        wm.stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id", equalTo(String.valueOf(planPolslId)))
                .withQueryParam("type", equalTo(String.valueOf(type)))
                .withQueryParam("wd", equalTo(String.valueOf(wd)))
                .withQueryParam("winH", equalTo(String.valueOf(PlanPolslClientProperties.WIN_W)))
                .withQueryParam("winW", equalTo(String.valueOf(PlanPolslClientProperties.WIN_H)))
                .willReturn(ok().withBody("""
                        <div class="cd">07:00-08:00</div>
                        <div class="coursediv" style="left: 40px; top: 30px;" cw="20" ch="10">
                            This is course div
                            <a href="https://site.com">Site</a>
                            <a href="https://other-site.com">Other site</a>
                        </div>
                        """))
        );

        // When
        scheduleEventProducer.send(SCHEDULE_EVENT_TOPIC, scheduleId, scheduleEvent);
        ConsumerRecord<String, RawScheduleEvent> result = KafkaTestUtils.getSingleRecord(rawScheduleConsumer, RAW_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldProduceProblematicEventOnDLT() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        int planPolslId = 2000;
        int type = 0;
        int wd = 4;

        Schedule schedule = Schedule.builder()
                .id(scheduleId)
                .planPolslId(planPolslId)
                .type(type)
                .wd(wd)
                .build();

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(schedule)
                .type(EventType.CREATE)
                .build();

        wm.stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id", equalTo(String.valueOf(planPolslId)))
                .withQueryParam("type", equalTo(String.valueOf(type)))
                .withQueryParam("wd", equalTo(String.valueOf(wd)))
                .withQueryParam("winH", equalTo(String.valueOf(PlanPolslClientProperties.WIN_W)))
                .withQueryParam("winW", equalTo(String.valueOf(PlanPolslClientProperties.WIN_H)))
                .willReturn(serverError())
        );

        // When
        scheduleEventProducer.send(SCHEDULE_EVENT_TOPIC, scheduleId, scheduleEvent);
        ConsumerRecord<String, ScheduleEvent> result = KafkaTestUtils.getSingleRecord(dltConsumer, DLT_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(scheduleEvent);
    }

    private void addCommonConsumerProps(Map<String, Object> consumerProps) {
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    }

}