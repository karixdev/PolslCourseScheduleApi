package com.example.discordnotificationservice.client;

import com.example.discordnotificationservice.dto.DiscordWebhookRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/webhooks")
public interface DiscordWebhookClient {
    @PostExchange("/{id}/{token}")
    ResponseEntity<Void> sendMessage(
            @PathVariable(name = "id") String id,
            @PathVariable(name = "token") String token,
            @RequestBody DiscordWebhookRequest message
    );
}
