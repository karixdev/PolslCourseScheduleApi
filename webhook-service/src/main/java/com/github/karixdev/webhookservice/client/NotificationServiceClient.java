package com.github.karixdev.webhookservice.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/notifications")
public interface NotificationServiceClient {
    @PostExchange("/{discordId}/{token}")
    ResponseEntity<Void> sendWelcomeMessage(
            @PathVariable(name = "discordId") String discordId,
            @PathVariable(name = "token") String token
    );
}
