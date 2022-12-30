package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.email.EmailService;
import com.github.karixdev.polslcoursescheduleapi.emailverification.exception.EmailAlreadyVerifiedException;
import com.github.karixdev.polslcoursescheduleapi.emailverification.exception.EmailVerificationTokenExpiredException;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationTokenRepository repository;
    private final Clock clock;
    private final EmailService emailService;
    private final EmailVerificationProperties properties;
    private final UserService userService;

    @Transactional
    public EmailVerificationToken createToken(User user) {
        LocalDateTime now = LocalDateTime.now(clock);
        String uuid = UUID.randomUUID().toString();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(uuid)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusHours(properties.getTokenExpirationHours()))
                .build();

        return repository.save(token);
    }

    public void sendEmailWithVerificationLink(EmailVerificationToken token) {
        Map<String, Object> variables = Map.of(
                "token", token.getToken()
        );

        String body = emailService.getMailTemplate("email-verification.html", variables);

        emailService.sendEmailToUser(token.getUser().getEmail(), "Verify your email", body);
    }

    @Transactional
    public SuccessResponse verify(String token) {
        EmailVerificationToken emailVerificationToken =
                repository.findByToken(token).orElseThrow(() -> {
                    throw new ResourceNotFoundException(
                            "Email verification token not found"
                    );
                });

        LocalDateTime now = LocalDateTime.now(clock);

        if (emailVerificationToken.getUser().getIsEnabled()) {
            throw new EmailAlreadyVerifiedException();
        }

        if (!now.isBefore(emailVerificationToken.getExpiresAt())) {
            throw new EmailVerificationTokenExpiredException();
        }

        userService.enableUser(emailVerificationToken.getUser());

        emailVerificationToken.setConfirmedAt(now);
        repository.save(emailVerificationToken);

        return new SuccessResponse();
    }
}
