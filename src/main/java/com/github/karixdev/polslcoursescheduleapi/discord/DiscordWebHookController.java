package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.discord.payload.request.DiscordWebHookRequest;
import com.github.karixdev.polslcoursescheduleapi.discord.payload.response.DiscordWebHookResponse;
import com.github.karixdev.polslcoursescheduleapi.security.CurrentUser;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/discord-web-hook")
@RequiredArgsConstructor
public class DiscordWebHookController {
    private final DiscordWebHookService service;

    @PostMapping
    public ResponseEntity<DiscordWebHookResponse> create(
            @Valid @RequestBody DiscordWebHookRequest payload,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return new ResponseEntity<>(
                service.create(payload, userPrincipal),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(
            @PathVariable(name = "id") Long id,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return new ResponseEntity<>(
                service.delete(id, userPrincipal),
                HttpStatus.OK
        );
    }
}
