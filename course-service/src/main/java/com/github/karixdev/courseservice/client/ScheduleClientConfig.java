package com.github.karixdev.courseservice.client;

import com.github.karixdev.courseservice.exception.ScheduleServiceException;
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

    @Bean
    ScheduleClient scheduleClient(
            @Value("${schedule-service.base-url}") String baseUrl
    ) {
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
                    throw new ScheduleServiceException(resp.statusCode());
                })
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(ScheduleClient.class);
    }
}
