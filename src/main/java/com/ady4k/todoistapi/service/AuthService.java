package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.AuthRequest;
import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.model.User;
import com.ady4k.todoistapi.repository.UserRepository;
import com.ady4k.todoistapi.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    public String loginByCredentials(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return JwtUtil.generateToken(new UserDto(user.getId(), user.getUsername(), user.getPassword()));
    }
}
