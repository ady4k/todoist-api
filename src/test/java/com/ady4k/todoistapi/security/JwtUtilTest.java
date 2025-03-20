package com.ady4k.todoistapi.security;

import com.ady4k.todoistapi.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {
    private final String secretKey = System.getenv("SECRET_KEY");
    private UserDto userDto;
    private String token;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "test", "pass");
        token = Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    @Test
    void generateToken_GivenUserDto_GeneratedCorrectToken() {
        // Act
        String generatedToken = JwtUtil.generateToken(userDto);

        // Assert
        assertThat(generatedToken).isNotNull();
        assertThat(JwtUtil.extractUsername(generatedToken)).isEqualTo(userDto.getUsername());
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        UserDetails userDetails = new User("test", "pass", Collections.emptyList());

        // Act
        boolean isValid = JwtUtil.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_InvalidToken_ReturnsFalse() {
        // Arrange
        UserDetails userDetails = new User("not_valid", "pass", Collections.emptyList());

        // Act
        boolean isValid = JwtUtil.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        UserDetails userDetails = new User("test", "pass", Collections.emptyList());

        // Act & Assert
        assertThrows(JwtException.class, () -> JwtUtil.isTokenValid(createExpiredToken(), userDetails));
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Act
        String username = JwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("test");
    }

    @Test
    void extractUsername_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> JwtUtil.extractUsername("invalid_token"));
        assertThrows(JwtException.class, () -> JwtUtil.extractUsername(createExpiredToken()));
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpiration() {
        // Act
        Date expiration = JwtUtil.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration.after(new Date())).isTrue();
    }

    @Test
    void extractExpiration_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> JwtUtil.extractExpiration("invalid_token"));
        assertThrows(JwtException.class, () -> JwtUtil.extractExpiration(createExpiredToken()));
    }

    @Test
    void extractAllClaims_ValidToken_ReturnsContent() {
        // Act
        Claims claims = JwtUtil.extractAllClaims(token);

        // Assert
        assertThat(claims.getSubject()).isEqualTo("test");
        assertThat(claims.getIssuedAt()).isBefore(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void extractAllClaims_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> JwtUtil.extractAllClaims("invalid_token"));
        assertThrows(JwtException.class, () -> JwtUtil.extractAllClaims(createExpiredToken()));
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 120))
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
}
