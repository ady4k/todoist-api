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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "test", "pass");
        user = new User(1L, "test", "pass");
    }

    @Test
    void getAllUsers_UsersInDb_ReturnsListOfUserDto() {
        List<UserDto> expected = List.of(
            new UserDto(1L, "test",  null),
            new UserDto(2L, "test2", null)
        );
        List<User> users = List.of(
            new User(1L, "test", "pass"),
            new User(2L, "test2", "pass2")
        );
        Mockito.when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expected);
    }

    @Test
    void getUserById_UserExists_ReturnCorrectUserDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void getUserById_UserNotExisting_Throw() {
        Mockito.when(userRepository.findById(1L)).thenThrow(new ResourceNotFoundException("User with id 1 not found"));
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L),
                "Expected getUserById(1) to throw but it didn't"
        );

        assertTrue(thrown.getMessage().contains("User with id 1 not found"));
    }

    @Test
    void getUserByUsername_UserExists_ReturnCorrectUserDto() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByUsername("test");

        assertNotNull(result);
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void getUserByUsername_UserNotExisting_Throw() {
        Mockito.when(userRepository.findByUsername("test")).thenThrow(new ResourceNotFoundException("User with username test not found"));

        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByUsername("test"),
                "Expected getUserByUsername(\"test\") to throw but it didn't"
        );
        assertTrue(thrown.getMessage().contains("User with username test not found"));
    }

    @Test
    void createUser_UsernameNotTaken_CreatesSuccessfully() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(passwordEncoder.encode("pass")).thenReturn("pass");

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertThat(result.getUsername()).isEqualTo("test");
    }

    @Test
    void createUser_UsernameIsTaken_Throw() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        AlreadyExistsException thrown = assertThrows(
                AlreadyExistsException.class,
                () -> userService.createUser(userDto),
                "Expected createUser(userDto) to throw but it didn't"
        );
        assertTrue(thrown.getMessage().contains("An user with the given username already exists"));
    }

    @Test
    void loadUserByUsername_UserExists_ReturnUserDetails() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("test");

        assertNotNull(result);
        assertThat(result.getUsername()).isEqualTo("test");
        assertThat(result.getPassword()).isEqualTo("pass");
    }

    @Test
    void loadUserByUsername_UserNotExisting_Throw() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.empty());

        UsernameNotFoundException thrown = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("test"),
                "Expected loadUserByUsername(\"test\") to throw but it didn't"
        );
        assertTrue(thrown.getMessage().contains("User not found"));
    }
}