package com.github.karixdev.courseservice.client;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.HttpServiceClientServerException;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class ScheduleClientConfig {

    private final WebClient.Builder webClientBuilder;

    private static final String SERVICE_NAME = "schedule-service";

    @Bean
    ScheduleClient scheduleClient(
            @Value("${schedule-service.base-url}") String baseUrl,
            ObservationRegistry observationRegistry
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
                    throw new HttpServiceClientServerException(SERVICE_NAME, resp.statusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
                    throw new HttpServiceClientException(SERVICE_NAME, resp.statusCode());
                })
                .observationRegistry(observationRegistry)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(ScheduleClient.class);
    }

}
