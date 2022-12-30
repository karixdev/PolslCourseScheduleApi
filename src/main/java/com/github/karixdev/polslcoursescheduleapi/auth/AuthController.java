package com.github.karixdev.polslcoursescheduleapi.auth;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.SignInRequest;
import com.github.karixdev.polslcoursescheduleapi.auth.response.SignInResponse;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(
            @Valid @RequestBody RegisterRequest payload
    ) {
        return new ResponseEntity<>(
                service.register(payload),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @Valid @RequestBody SignInRequest payload
    ) {
        return new ResponseEntity<>(
                service.signIn(payload),
                HttpStatus.OK
        );
    }
}
