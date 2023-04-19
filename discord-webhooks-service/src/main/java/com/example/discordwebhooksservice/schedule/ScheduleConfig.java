package com.example.discordwebhooksservice.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class ScheduleConfig {
    private final LoadBalancedExchangeFilterFunction filterFunction;

    @Bean
    ScheduleClient scheduleClient(
            @Value("${schedule-service.base-url}") String baseUrl
    ) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(filterFunction)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(ScheduleClient.class);
    }
}
