package com.github.karixdev.discordservice.config;

import com.github.karixdev.discordservice.client.DiscordWebhookClient;
import com.github.karixdev.discordservice.exception.DiscordClientException;
import com.github.karixdev.discordservice.exception.DiscordServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class DiscordWebhookClientConfig {
    @Bean
    DiscordWebhookClient discordApiClient(@Value("${discord-api.base-url}") String baseUrl) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
                    throw new DiscordServerException(resp.statusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
                    throw new DiscordClientException(resp.statusCode());
                })
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(DiscordWebhookClient.class);
    }
}
