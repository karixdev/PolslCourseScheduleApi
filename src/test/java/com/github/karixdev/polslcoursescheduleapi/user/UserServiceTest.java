package com.github.karixdev.polslcoursescheduleapi.user;

import com.github.karixdev.polslcoursescheduleapi.user.exception.EmailNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    UserService underTest;

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder passwordEncoder;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("email@email.com")
                .password("password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(Boolean.FALSE)
                .build();
    }

    @Test
    void GivenNotAvailableEmail_WhenCreateUser_ThenThrowsNotAvailableEmailExceptionWithCorrectMessage() {
        // Given
        String email = user.getEmail();
        String password = user.getPassword();
        UserRole userRole = user.getUserRole();
        Boolean isEnabled = user.getIsEnabled();

        when(repository.findByEmail(eq(email)))
                .thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> underTest.createUser(email, password, userRole, isEnabled))
                .isInstanceOf(EmailNotAvailableException.class)
                .hasMessage("Email is not available");
    }

    @Test
    void GivenValidCredentials_WhenCreateUser_ThenReturnsCorrectUser() {
        // Given
        String email = user.getEmail();
        String password = user.getPassword();
        UserRole userRole = user.getUserRole();
        Boolean isEnabled = user.getIsEnabled();

        when(passwordEncoder.encode(any()))
                .thenReturn(password);

        when(repository.save(eq(user)))
                .thenReturn(user);

        when(repository.findByEmail(eq(email)))
                .thenReturn(Optional.empty());

        // When
        User result = underTest.createUser(email, password, userRole, isEnabled);

        // Then
        assertThat(result).isEqualTo(user);
        verify(repository).save(eq(user));
    }

    @Test
    void GivenUser_WhenEnableUser_ThenSetsIsEnabledToTrue() {
        // Given
        user.setIsEnabled(Boolean.FALSE);

        // When
        underTest.enableUser(user);

        // Then
        assertThat(user.getIsEnabled()).isTrue();
        verify(repository).save(any());
    }
}
