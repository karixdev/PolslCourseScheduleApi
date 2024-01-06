package com.github.karixdev.webhookservice.config;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	WebClient.Builder proxyWebClient(LoadBalancedExchangeFilterFunction filterFunction) {
		return WebClient.builder().filter(filterFunction);
	}

}
