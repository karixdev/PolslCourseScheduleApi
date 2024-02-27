package com.github.karixdev.webscraperservice.application.event.handler;

import com.github.karixdev.webscraperservice.application.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.application.event.EventType;
import com.github.karixdev.webscraperservice.application.event.RawScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.ScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.producer.EventProducer;
import com.github.karixdev.webscraperservice.application.payload.PlanPolslResponse;
import com.github.karixdev.webscraperservice.application.props.PlanPolslClientProperties;
import com.github.karixdev.webscraperservice.application.scraper.PlanPolslResponseContentScraper;
import com.github.karixdev.webscraperservice.domain.RawCourse;
import com.github.karixdev.webscraperservice.domain.RawSchedule;
import com.github.karixdev.webscraperservice.domain.RawTimeInterval;
import com.github.karixdev.webscraperservice.domain.Schedule;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleEventHandlerTest {

    @InjectMocks
    ScheduleEventHandler underTest;

    @Mock
    PlanPolslClient planPolslClient;

    @Mock
    PlanPolslResponseContentScraper scraper;

    @Mock
    EventProducer<RawScheduleEvent> producer;

    @Test
    void GivenScheduleEventWithNotSupportedType_WhenHandle_ThenEventIsIgnored() {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .type(EventType.DELETE)
                .build();

        // When
        underTest.handle(event);

        // Then
        verify(planPolslClient, never()).getSchedule(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        verify(scraper, never()).scrapSchedule(any());
        verify(producer, never()).produce(any());
    }

    @ParameterizedTest
    @MethodSource("supportedEventTypes")
    void GivenScheduleEventWithSupportedType_WhenHandle_ThenScheduleIsScrapedAndRawScheduleEventIsBeingProduced(EventType type) {
        // Given
        Schedule schedule = Schedule.builder()
                .planPolslId(1)
                .type(2)
                .wd(3)
                .id(UUID.randomUUID().toString())
                .build();

        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(schedule.id())
                .entity(schedule)
                .type(type)
                .build();

        PlanPolslResponse planPolslResponse = PlanPolslResponse.builder()
                .content(Jsoup.parse(""))
                .build();

        when(planPolslClient.getSchedule(
                schedule.planPolslId(),
                schedule.type(),
                schedule.wd(),
                PlanPolslClientProperties.WIN_W,
                PlanPolslClientProperties.WIN_H
        )).thenReturn(planPolslResponse);

        RawCourse rawCourse = RawCourse.builder()
                .top(10)
                .left(20)
                .height(30)
                .width(40)
                .text("text")
                .build();

        RawTimeInterval rawTimeInterval = RawTimeInterval.builder()
                .start("07:00")
                .end("08:00")
                .build();

        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of(rawCourse))
                .timeIntervals(Set.of(rawTimeInterval))
                .build();

        when(scraper.scrapSchedule(planPolslResponse)).thenReturn(rawSchedule);

        RawScheduleEvent expectedEvent = RawScheduleEvent.builder()
                .scheduleId(schedule.id())
                .entity(rawSchedule)
                .build();

        // When
        underTest.handle(event);

        // Then
        verify(producer).produce(expectedEvent);
    }

    private static Stream<Arguments> supportedEventTypes() {
        return Stream.of(
                Arguments.of(EventType.CREATE),
                Arguments.of(EventType.UPDATE)
        );
    }

}