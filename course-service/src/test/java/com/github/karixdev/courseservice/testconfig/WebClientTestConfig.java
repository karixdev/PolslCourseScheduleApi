package com.github.karixdev.courseservice.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class WebClientTestConfig {

    @Primary
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

}
