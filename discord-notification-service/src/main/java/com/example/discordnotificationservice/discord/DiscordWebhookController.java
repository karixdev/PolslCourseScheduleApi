package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discord-webhooks")
@RequiredArgsConstructor
public class DiscordWebhookController {
    private final DiscordWebhookService service;

    @PostMapping
    ResponseEntity<DiscordWebhookResponse> create(
            @Valid @RequestBody DiscordWebhookRequest discordWebhookRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new ResponseEntity<>(
                service.create(discordWebhookRequest, jwt),
                HttpStatus.CREATED
        );
    }
}
