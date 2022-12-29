package com.github.karixdev.polslcoursescheduleapi.auth;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationService;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationToken;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public SuccessResponse register(RegisterRequest payload) {
        User user = userService.createUser(
                payload.getEmail(),
                payload.getPassword(),
                UserRole.ROLE_USER,
                false
        );

        EmailVerificationToken token =
                emailVerificationService.createToken(user);

        emailVerificationService.sendEmailWithVerificationLink(token);

        return new SuccessResponse();
    }
}
