package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.exception.AlreadyExistsException;
import com.ady4k.todoistapi.exception.ResourceNotFoundException;
import com.ady4k.todoistapi.model.User;
import com.ady4k.todoistapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private static final Duration CACHE_TTL = Duration.ofMinutes(60);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService<String, UserDto> cacheService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CacheService<String, UserDto> cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheService = cacheService;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        UserDto userDto = cacheService.getFromCache("USER:ID:" + id);
        if (userDto == null) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
            cacheService.addToCache("USER:ID:" + user.getId(), new UserDto(user.getId(), user.getUsername()), CACHE_TTL);
            return new UserDto(user.getId(), user.getUsername());
        }
        return userDto;
    }

    public UserDto getUserByUsername(String username) {
        UserDto userDto = cacheService.getFromCache("USER:USERNAME:" + username);
        if (userDto == null) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(("User with username " + username + " not found")));
        cacheService.addToCache("USER:USERNAME:" + username, new UserDto(user.getId(), user.getUsername()), CACHE_TTL);
        return new UserDto(user.getId(), user.getUsername());
        }
        return userDto;
    }

    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new AlreadyExistsException("An user with the given username already exists");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(user);

        UserDto newUserDto = new UserDto(savedUser.getId(), savedUser.getUsername());
        cacheService.addToCache("USER:ID:" + savedUser.getId(),newUserDto, CACHE_TTL);
        cacheService.addToCache("USER:USERNAME:" + savedUser.getUsername(), newUserDto, CACHE_TTL);

        return newUserDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
