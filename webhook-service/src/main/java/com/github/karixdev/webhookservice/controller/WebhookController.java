package com.github.karixdev.webhookservice.controller;

import com.github.karixdev.webhookservice.dto.WebhookRequest;
import com.github.karixdev.webhookservice.dto.WebhookResponse;
import com.github.karixdev.webhookservice.service.WebhookService;
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
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

	private final WebhookService service;

	@PostMapping
	ResponseEntity<WebhookResponse> create(
			@RequestBody @Valid WebhookRequest request,
			@AuthenticationPrincipal Jwt jwt
	) {
		return new ResponseEntity<>(service.create(request, jwt), HttpStatus.CREATED);
	}

}
