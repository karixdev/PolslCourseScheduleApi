package com.example.discordnotificationservice.discord;

import com.example.discordnotificationservice.discord.dto.DiscordWebhookRequest;
import com.example.discordnotificationservice.discord.dto.DiscordWebhookResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    ResponseEntity<Page<DiscordWebhookResponse>> findAll(
            @RequestParam(
                    name = "page",
                    required = false,
                    defaultValue = "0"
            ) Integer page,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new ResponseEntity<>(
                service.findAll(jwt, page),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        service.delete(id, jwt);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
