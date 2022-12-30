package com.github.karixdev.polslcoursescheduleapi.emailverification;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class EmailVerificationProperties {
    private final Integer tokenExpirationHours;
    private final Integer maxNumberOfMailsPerHour;

    public EmailVerificationProperties(
            @Value("${email-verification.expiration-hours}")
            Integer tokenExpirationHours,
            @Value("${email-verification.max-number-of-mails-per-hour}")
            Integer maxNumberOfMailsPerHour
    ) {
        this.tokenExpirationHours = tokenExpirationHours;
        this.maxNumberOfMailsPerHour = maxNumberOfMailsPerHour;
    }
}