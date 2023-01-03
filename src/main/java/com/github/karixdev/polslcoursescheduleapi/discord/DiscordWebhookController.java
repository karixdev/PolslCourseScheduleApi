package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebhookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebhookResponse;
import com.github.karixdev.polslcoursescheduleapi.security.CurrentUser;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/discord-webhook")
@RequiredArgsConstructor
public class DiscordWebhookController {
    private final DiscordWebhookService service;

    @PostMapping
    public ResponseEntity<DiscordWebhookResponse> create(
            @Valid @RequestBody DiscordWebhookRequest payload,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return new ResponseEntity<>(
                service.create(payload, userPrincipal),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(
            @PathVariable(name = "id") Long id,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return new ResponseEntity<>(
                service.delete(id, userPrincipal),
                HttpStatus.OK
        );
    }
}
