package com.example.discordnotificationservice.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final LoadBalancedExchangeFilterFunction filterFunction;

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(filterFunction);
    }
}
