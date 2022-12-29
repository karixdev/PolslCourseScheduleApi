package com.github.karixdev.polslcoursescheduleapi.emailverification;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class EmailVerificationProperties {
    private final Integer tokenExpirationHours;

    public EmailVerificationProperties(
            @Value("${email-verification.expiration-hours}")
            Integer tokenExpirationHours
    ) {
        this.tokenExpirationHours = tokenExpirationHours;
    }
}