package com.ady4k.todoistapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidToken() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        String username = "test";
        String authHeader = "Bearer " + token;

        UserDetails userDetails = User.withUsername(username).password("password").roles("USER").build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.extractUsername(any(String.class))).thenReturn(username);
            jwtUtil.when(() -> JwtUtil.isTokenValid(any(String.class), any(UserDetails.class))).thenReturn(true);

            // Act
            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, times(1)).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
        }
    }

    @Test
    void testDoFilterInternal_WithInvalidToken() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;

        try (MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.extractUsername(any(String.class))).thenReturn("testuser");
            jwtUtil.when(() -> JwtUtil.isTokenValid(any(String.class), any(UserDetails.class))).thenReturn(false);
            jwtUtil.when(() -> request.getHeader("Authorization")).thenReturn(authHeader);

            // Act
            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, times(1)).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Test
    void testDoFilterInternal_WhenNoAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_WithInvalidBearerToken() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String authHeader = "NotBearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
