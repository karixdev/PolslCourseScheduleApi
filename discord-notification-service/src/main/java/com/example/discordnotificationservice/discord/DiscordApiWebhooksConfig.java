package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.exception.DiscordApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class DiscordApiWebhooksConfig {
    @Bean
    DiscordApiWebhooksClient discordApiClient(@Value("${discord-api.base-url}") String baseUrl) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, resp -> {
                    throw new DiscordApiException();
                })
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(DiscordApiWebhooksClient.class);
    }
}