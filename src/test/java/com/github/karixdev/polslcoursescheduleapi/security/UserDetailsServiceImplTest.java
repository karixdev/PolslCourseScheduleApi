package com.github.karixdev.polslcoursescheduleapi.security;

import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @InjectMocks
    UserDetailsServiceImpl underTest;

    @Mock
    UserService userService;

    @Test
    void GivenNotExistingUserEmail_WhenLoadByUsername_ThenThrowsUsernameNotFoundException() {
        // Given
        String email = "i-do-not-exist@email.com";

        when(userService.findByEmail(any()))
                .thenThrow(new ResourceNotFoundException(
                        "User with provided email not found"));

        // When & Then
        assertThatThrownBy(() -> underTest.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User with provided email not found");
    }

    @Test
    void GivenExistingUserEmail_WhenLoadByUsername_ThenReturnsCorrectUserDetails() {
        // Given
        String email = "email@email.com";

        when(userService.findByEmail(any()))
                .thenReturn(User.builder()
                        .email("email@email.com")
                        .password("password")
                        .userRole(UserRole.ROLE_USER)
                        .isEnabled(Boolean.TRUE)
                        .build());

        // When
        UserDetails result = underTest.loadUserByUsername(email);

        // Then
        assertThat(result.getUsername()).isEqualTo("email@email.com");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.getAuthorities()).hasSize(1);
    }
}
