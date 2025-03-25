package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class TokenService {
    private static final int TIME_LIMIT = 5 * 60 * 1000;
    private final CacheService<String, String> cacheService;
    private final UserDetailsService userDetailsService;

    private final Duration cacheExpiration = Duration.ofMillis(Integer.parseInt(System.getenv("TOKEN_EXPIRATION_TIME_MILLIS")));

    public TokenService(CacheService<String, String> cacheService, UserDetailsService userDetailsService) {
        this.cacheService = cacheService;
        this.userDetailsService = userDetailsService;
    }

    public String getOrCreateToken(UserDto userDto) {
        String existingToken = cacheService.getFromCache(userDto.getUsername());

        if (existingToken != null) {
            if (getRemainingTime(existingToken) > TIME_LIMIT) {
                return existingToken;
            } else {
                this.invalidateToken(userDto);
            }
        }

        String createdToken = JwtUtil.generateToken(userDto);
        cacheService.addToCache(userDto.getUsername(), createdToken, cacheExpiration);

        return createdToken;
    }

    public void invalidateToken(UserDto userDto) {
        cacheService.removeFromCache(userDto.getUsername());
    }

    public boolean isTokenValid(String token, UserDto userDto) {
        if (cacheService.getFromCache(userDto.getUsername()) != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDto.getUsername());
            return JwtUtil.isTokenValid(token, userDetails);
        }
        return false;
    }

    private long getRemainingTime(String token) {
        Date now = new Date();
        return now.getTime() - JwtUtil.extractExpiration(token).getTime();
    }
}
