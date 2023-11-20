package com.github.karixdev.webscraperservice;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.RawCourse;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.course.raw.Link;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.webscraperservice.props.PlanPolslClientProperties;
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
    Consumer<String, RawCourse> rawCourseConsumer;
    Consumer<String, ScheduleEvent> dltConsumer;

    WireMockServer wm;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, ScheduleEvent> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        scheduleEventProducer = new KafkaTemplate<>(producerFactory);

        Map<String, Object> rawCourseConsumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "raw-course-test-group", "false");
        addCommonConsumerProps(rawCourseConsumerProps);

        ConsumerFactory<String, RawCourse> rawCourseConsumerFactory = new DefaultKafkaConsumerFactory<>(rawCourseConsumerProps);
        rawCourseConsumer = rawCourseConsumerFactory.createConsumer();
        rawCourseConsumer.subscribe(List.of(RAW_TOPIC));

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
        rawCourseConsumer.close();
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

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .planPolslId(planPolslId)
                .eventType(EventType.CREATE)
                .scheduleId(scheduleId)
                .type(type)
                .wd(wd)
                .build();

        TimeCell timeCell = new TimeCell("07:00-08:00");
        CourseCell courseCell = CourseCell.builder()
                .top(30)
                .left(40)
                .ch(10)
                .cw(20)
                .text("This is course div")
                .links(Set.of(
                        new Link("Site", "https://site.com"),
                        new Link("Other site", "https://other-site.com")
                ))
                .build();

        RawCourse expectedRawCourse = RawCourse.builder()
                .scheduleId(scheduleId)
                .timeCells(Set.of(timeCell))
                .courseCells(Set.of(courseCell))
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
        ConsumerRecord<String, RawCourse> result = KafkaTestUtils.getSingleRecord(rawCourseConsumer, RAW_TOPIC, Duration.ofSeconds(20));

        // Then
        assertThat(result.key()).isEqualTo(scheduleId);
        assertThat(result.value()).isEqualTo(expectedRawCourse);
    }

    @Test
    void shouldProduceProblematicEventOnDLT() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        int planPolslId = 2000;
        int type = 0;
        int wd = 4;

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .planPolslId(planPolslId)
                .eventType(EventType.CREATE)
                .scheduleId(scheduleId)
                .type(type)
                .wd(wd)
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