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
        return proxyFactory(baseUrl).createClient(ScheduleClient.class);
    }

    @Bean
    NotificationServiceClient notificationServiceClient(
            @Value("${notification-service.base-url}") String baseUrl
    ) {
        return proxyFactory(baseUrl).createClient(NotificationServiceClient.class);
    }

    private HttpServiceProxyFactory proxyFactory(String baseUrl) {
        WebClient webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
                    throw new ServiceServerException(resp.statusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
                    throw new ServiceClientException(resp.statusCode());
                })
                .build();

        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
    }
}
