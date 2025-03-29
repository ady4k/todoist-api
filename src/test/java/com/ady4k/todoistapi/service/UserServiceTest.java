package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.exception.AlreadyExistsException;
import com.ady4k.todoistapi.exception.ResourceNotFoundException;
import com.ady4k.todoistapi.model.User;
import com.ady4k.todoistapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MultiLayerCacheService cacheService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "test", "pass");
        user = new User(1L, "test", "pass");
    }

    @Test
    void getAllUsers_UsersInDb_ReturnsListOfUserDto() {
        // Arrange
        List<UserDto> expected = List.of(
                new UserDto(1L, "test", null),
                new UserDto(2L, "test2", null)
        );
        List<User> users = List.of(
                new User(1L, "test", "pass"),
                new User(2L, "test2", "pass2")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull().hasSize(2).containsExactlyElementsOf(expected);
    }

    @Test
    void getUserById_UserExists_ReturnCorrectUserDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void getUserById_UserNotExisting_Throw() {
        // Arrange
        when(userRepository.findById(1L)).thenThrow(new ResourceNotFoundException("User with id 1 not found"));

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L),
                "Expected getUserById(1) to throw but it didn't"
        );
        assertThat(thrown.getMessage().contains("User with id 1 not found")).isTrue();
    }

    @Test
    void getUserByUsername_UserExists_ReturnCorrectUserDto() {
        // Arrange
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.getUserByUsername("test");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void getUserByUsername_UserNotExisting_Throw() {
        // Arrange
        when(userRepository.findByUsername("test")).thenThrow(new ResourceNotFoundException("User with username test not found"));

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByUsername("test"),
                "Expected getUserByUsername(\"test\") to throw but it didn't"
        );
        assertThat(thrown.getMessage().contains("User with username test not found")).isTrue();
    }

    @Test
    void createUser_UsernameNotTaken_CreatesSuccessfully() {
        // Arrange
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("pass");

        // Act
        UserDto result = userService.createUser(userDto);

        // Arrange
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void createUser_UsernameIsTaken_Throw() {
        // Arrange
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        // Act & Assert
        AlreadyExistsException thrown = assertThrows(
                AlreadyExistsException.class,
                () -> userService.createUser(userDto),
                "Expected createUser(userDto) to throw but it didn't"
        );
        assertThat(thrown.getMessage().contains("An user with the given username already exists")).isTrue();
    }

    @Test
    void loadUserByUsername_UserExists_ReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = userService.loadUserByUsername("test");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test");
        assertThat(result.getPassword()).isEqualTo("pass");
    }

    @Test
    void loadUserByUsername_UserNotExisting_Throw() {
        // Arrange
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException thrown = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("test"),
                "Expected loadUserByUsername(\"test\") to throw but it didn't"
        );
        assertThat(thrown.getMessage().contains("User not found")).isTrue();
    }
}