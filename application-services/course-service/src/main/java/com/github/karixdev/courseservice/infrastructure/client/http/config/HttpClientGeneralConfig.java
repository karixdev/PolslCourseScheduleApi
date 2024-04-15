package com.github.karixdev.courseservice.infrastructure.client.http.config;

import com.github.karixdev.courseservice.infrastructure.client.http.logger.WebClientResponseExceptionInfoLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientGeneralConfig{

    @Bean
    WebClientResponseExceptionInfoLogger webClientResponseExceptionInfoLogger() {
        return new WebClientResponseExceptionInfoLogger();
    }

}
