package com.github.karixdev.webhookservice.config;

import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.HttpServiceClientServerException;
import com.github.karixdev.webhookservice.client.DiscordWebhookClient;
import io.micrometer.observation.ObservationRegistry;
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
	DiscordWebhookClient discordWebhookClient(
			@Value("${discord-webhook.base-url}") String baseUrl,
			ObservationRegistry observationRegistry
	) {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.observationRegistry(observationRegistry)
				.defaultStatusHandler(HttpStatusCode::is5xxServerError, resp -> {
					throw new HttpServiceClientServerException("schedule-service", resp.statusCode());
				})
				.defaultStatusHandler(HttpStatusCode::is4xxClientError, resp -> {
					throw new HttpServiceClientException("schedule-service", resp.statusCode());
				})
				.build();

		HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
				.builder(WebClientAdapter.forClient(webClient))
				.build();

		return proxyFactory.createClient(DiscordWebhookClient.class);
	}

}
