package com.github.karixdev.webscraperservice.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    WebClient webClient(@Value("${plan-polsl-url}") String planPolslUrl) {
        return WebClient.builder()
                .baseUrl(planPolslUrl)
                .build();
    }
}