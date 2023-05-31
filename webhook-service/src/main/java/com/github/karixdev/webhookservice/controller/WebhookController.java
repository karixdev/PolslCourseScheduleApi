package com.github.karixdev.webhookservice.controller;

import com.github.karixdev.webhookservice.dto.WebhookRequest;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
import com.github.karixdev.webhookservice.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService service;

    @PostMapping
    ResponseEntity<WebhookResponse> create(
            @Valid @RequestBody WebhookRequest discordWebhookRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new ResponseEntity<>(
                service.create(discordWebhookRequest, jwt),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    ResponseEntity<Page<WebhookResponse>> findAll(
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

    @PutMapping("/{id}")
    ResponseEntity<WebhookResponse> update(
            @Valid @RequestBody WebhookRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        return new ResponseEntity<>(
                service.update(request, jwt, id),
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
