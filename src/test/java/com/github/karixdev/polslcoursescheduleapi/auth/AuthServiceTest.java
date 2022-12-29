package com.github.karixdev.polslcoursescheduleapi.auth;

import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    AuthService underTest;

    @Mock
    UserService userService;

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
    }
}
