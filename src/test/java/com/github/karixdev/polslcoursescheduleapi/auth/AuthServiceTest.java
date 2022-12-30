package com.github.karixdev.polslcoursescheduleapi.auth;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.SignInRequest;
import com.github.karixdev.polslcoursescheduleapi.auth.response.SignInResponse;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationService;
import com.github.karixdev.polslcoursescheduleapi.emailverification.EmailVerificationToken;
import com.github.karixdev.polslcoursescheduleapi.jwt.JwtService;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    AuthService underTest;

    @Mock
    UserService userService;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;

    User user;
    EmailVerificationToken token;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("email@email.com")
                .password("password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(Boolean.TRUE)
                .build();

        token = EmailVerificationToken.builder()
                .token("random")
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    void GivenRegisterRequest_WhenRegister_ThenCallsUserServiceCreateUserAndReturnsSuccessResponse() {
        // Given
        RegisterRequest payload =
                new RegisterRequest("abc@abc.pl", "password");

        // When
        SuccessResponse result = underTest.register(payload);

        // Then
        assertThat(result.getMessage()).isEqualTo("success");

        verify(userService).createUser(
                eq("abc@abc.pl"),
                eq("password"),
                eq(UserRole.ROLE_USER),
                eq(false)
        );

        verify(emailVerificationService).createToken(any());
    }

    @Test
    void GivenSignInRequest_WhenSignIn_ThenReturnsCorrectSignInResponse() {
        // Given
        SignInRequest payload =
                new SignInRequest("email@email.com", "password");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(user),
                        "email@email.com:password"
                ));

        when(jwtService.createToken(any()))
                .thenReturn("token");

        // When
        SignInResponse result = underTest.signIn(payload);

        // Then
        assertThat(result.getAccessToken()).isEqualTo("token");

        assertThat(result.getUserResponse().getIsEnabled()).isTrue();
        assertThat(result.getUserResponse().getUserRole())
                .isEqualTo(UserRole.ROLE_USER);
        assertThat(result.getUserResponse().getEmail())
                .isEqualTo("email@email.com");
    }
}
