package com.ady4k.todoistapi.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFound() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertThat(HttpStatus.NOT_FOUND).isEqualTo(response.getStatusCode());
        assertThat("Resource not found").isEqualTo(response.getBody());
    }

    @Test
    void testHandleAlreadyExists() {
        // Arrange
        AlreadyExistsException exception = new AlreadyExistsException("Already exists");

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleAlreadyExists(exception);

        // Assert
        assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());
        assertThat("Already exists").isEqualTo(response.getBody());
    }
}
