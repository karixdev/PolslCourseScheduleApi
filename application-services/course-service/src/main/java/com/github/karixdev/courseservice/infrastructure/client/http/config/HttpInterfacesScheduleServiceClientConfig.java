package com.github.karixdev.courseservice.infrastructure.client.http.config;

import com.github.karixdev.courseservice.infrastructure.client.http.HttpInterfacesScheduleServiceClient;
import com.github.karixdev.courseservice.infrastructure.client.http.decorator.HttpInterfacesScheduleServiceClientResponseExceptionLoggerDecorator;
import com.github.karixdev.courseservice.infrastructure.client.http.logger.WebClientResponseExceptionInfoLogger;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfacesScheduleServiceClientConfig {

    @Bean
    HttpInterfacesScheduleServiceClient httpInterfacesScheduleServiceClient(
            @Value("${schedule-service.base-url}") String baseUrl,
            WebClient.Builder webClientBuilder,
            ObservationRegistry observationRegistry,
            WebClientResponseExceptionInfoLogger exceptionInfoLogger
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        HttpInterfacesScheduleServiceClient client = factory.createClient(HttpInterfacesScheduleServiceClient.class);

        return new HttpInterfacesScheduleServiceClientResponseExceptionLoggerDecorator(client, exceptionInfoLogger);
    }

}
