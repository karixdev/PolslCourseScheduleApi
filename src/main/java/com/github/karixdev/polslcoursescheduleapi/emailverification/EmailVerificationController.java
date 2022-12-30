package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
