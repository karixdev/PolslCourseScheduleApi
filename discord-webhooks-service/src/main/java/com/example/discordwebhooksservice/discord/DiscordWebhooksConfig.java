package com.example.discordwebhooksservice.discord;

import com.example.discordwebhooksservice.discord.exception.DiscordApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class DiscordWebhooksConfig {
    @Bean
    DiscordWebhooksClient discordApiClient(@Value("${discord-api.base-url}") String baseUrl) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, resp -> {
                    throw new DiscordApiException();
                })
                .build();

        System.out.println(baseUrl);

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builder(WebClientAdapter.forClient(webClient))
                        .build();

        return factory.createClient(DiscordWebhooksClient.class);
    }
}
