package com.github.karixdev.courseservice.client;

import com.github.karixdev.courseservice.exception.ScheduleServiceClientException;
import com.github.karixdev.courseservice.exception.ScheduleServiceServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

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
                    throw new ScheduleServiceServerException(resp.statusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
                    throw new ScheduleServiceClientException(resp.statusCode());
                })
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(ScheduleClient.class);
    }
}
