package com.github.karixdev.courseservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientBuilderConfig {
    private final LoadBalancedExchangeFilterFunction filterFunction;

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(filterFunction);
    }
}