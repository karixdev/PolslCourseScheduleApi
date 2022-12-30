package com.github.karixdev.polslcoursescheduleapi.jwt;

import com.github.karixdev.polslcoursescheduleapi.security.UserDetailsServiceImpl;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {
    @InjectMocks
    JwtAuthFilter underTest;

    @Mock
    UserDetailsServiceImpl userDetailsService;

    @Mock
    JwtService jwtService;

    @Mock
    HttpServletResponse servletResponse;

    @Mock
    FilterChain filterChain;

    @Mock
    SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void GivenRequestWithoutAuthorizationHeader_WhenDoFilterInternal_ThenAuthenticationIsNotProcessed() throws Exception {
        // Given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        // When
        underTest.doFilterInternal(servletRequest, servletResponse, filterChain);

        // Then
        verify(userDetailsService, never()).loadUserByUsername(any(String.class));
        verify(filterChain).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class));
    }

    @Test
    void GivenRequestWithInvalidToken_WhenDoFilterInternal_ThenAuthenticationIsNotProcessed() throws Exception {
        // Given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Authorization", "Bearer token");

        when(jwtService.isTokenValid(any(String.class)))
                .thenReturn(false);

        // When
        underTest.doFilterInternal(servletRequest, servletResponse, filterChain);

        // Then
        verify(userDetailsService, never()).loadUserByUsername(any(String.class));
        verify(filterChain).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class));
    }

    @Test
    void GivenRequestWithValidToken_WhenDoFilterInternal_ThenAuthenticationIsProcessed() throws Exception {
        // Given
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Authorization", "Bearer token");

        when(jwtService.isTokenValid(any()))
                .thenReturn(true);

        when(jwtService.getEmailFromToken(any()))
                .thenReturn("email@email.com");

        UserDetails userDetails = new UserPrincipal(User.builder()
                .email("email@email.com")
                .password("password")
                .userRole(UserRole.ROLE_USER)
                .isEnabled(true)
                .build());

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(userDetails);

        // When
        underTest.doFilterInternal(servletRequest, servletResponse, filterChain);

        // Then
        verify(userDetailsService).loadUserByUsername(any());
        verify(SecurityContextHolder.getContext()).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(
                any(ServletRequest.class),
                any(ServletResponse.class));
    }
}
