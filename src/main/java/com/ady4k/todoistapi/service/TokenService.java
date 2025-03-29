package com.ady4k.todoistapi.service;

import com.ady4k.todoistapi.dto.UserDto;
import com.ady4k.todoistapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class TokenService {
    private static final int TIME_LIMIT = 5 * 60 * 1000;
    private final JwtUtil jwtUtil;
    private final RedisCacheService<String> redisCacheService;
    private final UserDetailsService userDetailsService;
    private final Duration cacheTtl;

    public TokenService(JwtUtil jwtUtil, RedisCacheService<String> redisCacheService,
                        UserDetailsService userDetailsService,
                        @Value("${token.expiration.time.minutes}") String tokenExpirationTimeMinutes) {
        this.jwtUtil = jwtUtil;
        this.redisCacheService = redisCacheService;
        this.userDetailsService = userDetailsService;
        this.cacheTtl = Duration.ofMinutes(Long.parseLong(tokenExpirationTimeMinutes));
    }

    public String getOrCreateToken(UserDto userDto) {
        String existingToken = redisCacheService.get(userDto.getUsername());

        if (existingToken != null) {
            if (getRemainingTime(existingToken) > TIME_LIMIT) {
                return existingToken;
            } else {
                this.invalidateToken(userDto);
            }
        }

        String createdToken = jwtUtil.generateToken(userDto);
        redisCacheService.put(userDto.getUsername(), createdToken, cacheTtl);

        return createdToken;
    }

    public void invalidateToken(UserDto userDto) {
        redisCacheService.evict(userDto.getUsername());
    }

    public boolean isTokenValid(String token, UserDto userDto) {
        if (redisCacheService.get(userDto.getUsername()) != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDto.getUsername());
            return jwtUtil.isTokenValid(token, userDetails);
        }
        return false;
    }

    private long getRemainingTime(String token) {
        Date now = new Date();
        return now.getTime() - jwtUtil.extractExpiration(token).getTime();
    }
}
