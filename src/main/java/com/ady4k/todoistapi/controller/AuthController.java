package com.ady4k.todoistapi.controller;

import com.ady4k.todoistapi.dto.AuthRequest;
import com.ady4k.todoistapi.dto.AuthResponse;
import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.service.AuthService;
import com.ady4k.todoistapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        String token = authService.loginByCredentials(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Validated AuthRequest request) {
        UserDto createdUser = userService.createUser(new UserDto(request.getUsername(), request.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser.toString());
    }
}
