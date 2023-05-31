package com.github.karixdev.notificationservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {
    ApiKeyFilter underTest = new ApiKeyFilter("api-key");

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Test
    void GivenRequestWithNoApiKey_WhenDoFilterInternal_ThenSetsUnauthorizedStatus() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // When
        underTest.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void GivenRequestWithInvalidApiKey_WhenDoFilterInternal_ThenSetsUnauthorizedStatus() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-KEY", "invalid");

        // When
        underTest.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void GivenRequestWithValidApiKey_WhenDoFilterInternal_ThenContinuesFilterChain() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-KEY", "api-key");

        // When
        underTest.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(
                eq(request),
                eq(response)
        );
    }
}