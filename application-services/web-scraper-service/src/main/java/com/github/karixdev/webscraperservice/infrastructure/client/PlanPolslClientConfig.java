package com.github.karixdev.webscraperservice.infrastructure.client;

import com.github.karixdev.webscraperservice.application.client.PlanPolslClient;
import com.github.karixdev.webscraperservice.infrastructure.client.exception.PlanPolslUnavailableException;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class PlanPolslClientConfig {

    WebClient webClient(
            String planPolslUrl,
            ObservationRegistry observationRegistry
    ) {
        return WebClient.builder()
                .baseUrl(planPolslUrl)
                .defaultStatusHandler(HttpStatusCode::isError, response -> {
                    int statusCode = response.statusCode().value();
                    throw new PlanPolslUnavailableException(statusCode);
                })
                .observationRegistry(observationRegistry)
                .build();
    }

    HttpInterfacesPlanPolslClient httpInterfacesPlanPolslClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(HttpInterfacesPlanPolslClient.class);
    }

    @Bean
    PlanPolslClient planPolslClient(
            @Value("${plan-polsl-url}") String planPolslUrl,
            ObservationRegistry observationRegistry
    ) {
        WebClient webClient = webClient(planPolslUrl, observationRegistry);
        HttpInterfacesPlanPolslClient httpInterfacesClient = httpInterfacesPlanPolslClient(webClient);

        return new PlanPolslClientAdapter(httpInterfacesClient);
    }

}
