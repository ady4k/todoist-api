package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.AuthRequest;
import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.model.User;
import com.ady4k.todoistapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    private User user;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test", "pass");
        authRequest = new AuthRequest("test", "pass");
    }

    @Test
    void loginByCredentials_AuthenticatesSuccessfully_ReturnsToken() {
        // Arrange
        String expected = "token";
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(tokenService.getOrCreateToken(any(UserDto.class))).thenReturn(expected);

        // Act
        String result = authService.loginByCredentials(authRequest);

        // Assert
        assertThat(result).isNotNull().isEqualTo(expected);
    }

    @Test
    void loginByCredentials_UsernameNotExisting_Throws() {
        // Arrange
        when(userRepository.findByUsername(any(String.class))).thenThrow(new NoSuchElementException("Missing user"));

        // Act & Assert
        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> authService.loginByCredentials(authRequest),
                "Expected authService.loginByCredentials(authRequest) to throw but it didn't"
        );
        assertThat(thrown).hasMessageContaining("Missing user");
    }

    @Test
    void loginByCredentials_WrongCredentials_Throws() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Wrong credentials"));

        // Act & Assert
        BadCredentialsException thrown = assertThrows(
                BadCredentialsException.class,
                () -> authService.loginByCredentials(authRequest),
                "Expected authService.loginByCredentials(authRequest) to throw but it didn't"
        );
        assertThat(thrown).hasMessageContaining("Wrong credentials");
    }
}
