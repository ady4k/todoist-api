package com.ady4k.todoistapi.controller;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.exception.ResourceNotFoundException;
import com.ady4k.todoistapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "test", "pass");
    }

    @Test
    void getAllUsers_ReturnsUserList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        // Act
        ResponseEntity<List<UserDto>> response = userController.getAllUsers();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().getFirst()).isEqualTo(userDto);
    }

    @Test
    void getUserById_ValidId_ReturnsUser() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userDto);

        // Act
        ResponseEntity<UserDto> response = userController.getUserById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_NonExistentId_ThrowsException() {
        // Arrange & Act
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found"));
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> userController.getUserById(99L),
                "Expected getUserById(99L) to throw but it didn't"
        );

        // Assert
        assertThat(thrown).hasMessageContaining("User not found");
    }
}
