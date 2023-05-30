package com.example.discordnotificationservice.config;

import com.example.discordnotificationservice.client.NotificationServiceClient;
import com.example.discordnotificationservice.client.ScheduleClient;
import com.example.discordnotificationservice.exception.client.ServiceClientException;
import com.example.discordnotificationservice.exception.client.ServiceServerException;
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
public class ServiceClientConfig {
    private final WebClient.Builder webClientBuilder;

    @Bean
    ScheduleClient scheduleClient(
            @Value("${schedule-service.base-url}") String baseUrl
    ) {
        return proxyFactory(baseUrl, null).createClient(ScheduleClient.class);
    }

    @Bean
    NotificationServiceClient notificationServiceClient(
            @Value("${notification-service.base-url}") String baseUrl,
            @Value("${notification-service.api-key}") String apiKey
    ) {
        return proxyFactory(baseUrl, apiKey).createClient(NotificationServiceClient.class);
    }

    private HttpServiceProxyFactory proxyFactory(String baseUrl, String apiKey) {
        WebClient.Builder builder = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
                    throw new ServiceServerException(resp.statusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
                    throw new ServiceClientException(resp.statusCode());
                });

        if (apiKey != null) {
            builder.defaultHeader("X-API-KEY", apiKey);
        }

        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(builder.build()))
                .build();
    }
}
