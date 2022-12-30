package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.emailverification.request.ResendEmailVerificationTokenRequest;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService service;

    @PostMapping("/{token}")
    public ResponseEntity<SuccessResponse> verify(
            @PathVariable(name = "token") String token
    ) {
        return new ResponseEntity<>(
                service.verify(token),
                HttpStatus.OK
        );
    }

    @PostMapping("/resend")
    public ResponseEntity<SuccessResponse> resend(
            @Valid @RequestBody ResendEmailVerificationTokenRequest payload
    ) {
        return new ResponseEntity<>(
                service.resend(payload),
                HttpStatus.OK
        );
    }
}
