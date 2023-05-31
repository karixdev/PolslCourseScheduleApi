package com.github.karixdev.notificationservice.controller;

import com.github.karixdev.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PostMapping("/{discordId}/{token}")
    ResponseEntity<Void> welcomeMessage(
            @PathVariable(name = "discordId") String discordId,
            @PathVariable(name = "token") String token
    ) {
        service.sendWelcomeMessage(discordId, token);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
