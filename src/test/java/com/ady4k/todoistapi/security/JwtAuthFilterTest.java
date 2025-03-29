package com.ady4k.todoistapi.security;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.PrintWriter;

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
    private TokenService tokenService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtUtil jwtUtil;

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
        when(tokenService.isTokenValid(any(String.class), any(UserDto.class))).thenReturn(true);
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(any(String.class))).thenReturn(username);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
    }

    @Test
    void testDoFilterInternal_WithAuthHeaderAlreadyExisting() throws Exception {
        // Arrange
        UserDetails userDetails = User.withUsername("test").password("password").roles("USER").build();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithInvalidToken() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);
        when(jwtUtil.extractUsername(any(String.class))).thenReturn("testuser");

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).getWriter();
        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_WhenNoAuthorizationHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).getWriter();
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_WithInvalidBearerToken() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String authHeader = "NotBearer " + token;
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockWriter);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).getWriter();
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
