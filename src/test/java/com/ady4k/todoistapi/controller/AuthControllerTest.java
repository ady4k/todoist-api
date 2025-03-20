package com.ady4k.todoistapi.controller;

import com.ady4k.todoistapi.dto.AuthRequest;
import com.ady4k.todoistapi.dto.AuthResponse;
import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.exception.AlreadyExistsException;
import com.ady4k.todoistapi.service.AuthService;
import com.ady4k.todoistapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    private AuthRequest authRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("test", "pass");
        userDto = new UserDto(1L, "test", "pass");
    }

    @Test
    void login_ValidCredentials_ReturnsToken() {
        // Arrange
        String expected = "token";
        when(authService.loginByCredentials(any(AuthRequest.class))).thenReturn(expected);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo(expected);
    }

    @Test
    void login_InvalidCredentials_Throws() {
        // Arrange & Act
        when(authService.loginByCredentials(any(AuthRequest.class))).thenThrow(new BadCredentialsException("Wrong credentials"));
        BadCredentialsException thrown = assertThrows(
                BadCredentialsException.class,
                () -> authService.loginByCredentials(authRequest),
                "Expected authService.loginByCredentials(authRequest) to throw but it didn't"
        );

        // Assert
        assertThat(thrown).hasMessageContaining("Wrong credentials");
    }

    @Test
    void register_ValidUser_ReturnsCreatedUser() {
        // Arrange
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        // Act
        ResponseEntity<String> response = authController.register(authRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(userDto.toString());
    }

    @Test
    void register_DuplicateUser_ThrowsException() {
        // Arrange & Act
        when(userService.createUser(any(UserDto.class))).thenThrow(new AlreadyExistsException("Username already exists"));
        AlreadyExistsException thrown = assertThrows(
                AlreadyExistsException.class,
                () -> authController.register(authRequest),
                "Expected createUser(userDto) to throw but it didn't"
        );

        // Assert
        assertThat(thrown).hasMessageContaining("Username already exists");
    }
}
