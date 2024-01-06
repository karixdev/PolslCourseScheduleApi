package com.github.karixdev.webhookservice.client;

import com.github.karixdev.webhookservice.dto.DiscordWebhookRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/webhooks")
public interface DiscordWebhookClient {

	@PostExchange("/{id}/{token}")
	void send(
			@PathVariable(name = "id") String id,
			@PathVariable(name = "token") String token,
			@RequestBody DiscordWebhookRequest request
	);

}
