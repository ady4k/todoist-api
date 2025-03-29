package com.ady4k.todoistapi.security;

import com.ady4k.todoistapi.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {
    private static final String TEST_SECRET_KEY = "test-secret-key-needs-to-be-at-least-32-bytes-long-for-hs256";
    private static final long TEST_EXPIRATION_MS = 60 * 60 * 1000; // 1 hour in ms
    private static final SecretKey TEST_SIGNING_KEY = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes());

    private JwtUtil jwtUtil;

    private UserDto userDto;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET_KEY, TEST_EXPIRATION_MS);
        jwtUtil.init();

        userDto = new UserDto(1L, "user", "pass");

        validToken = jwtUtil.generateToken(userDto);
        expiredToken = Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - TEST_EXPIRATION_MS * 2)) // Issued 2h ago
                .expiration(new Date(System.currentTimeMillis() - TEST_EXPIRATION_MS)) // Expired 1h ago
                .signWith(TEST_SIGNING_KEY)
                .compact();
    }

    @Test
    void generateToken_GivenUserDto_GeneratedCorrectToken() {
        // Act
        Claims claims = jwtUtil.extractAllClaims(validToken);

        // Assert
        assertThat(validToken).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userDto.getUsername());
        assertThat(claims.getExpiration()).isAfter(new Date());
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        UserDetails userDetails = new User("user", "pass", Collections.emptyList());

        // Act
        boolean isValid = jwtUtil.isTokenValid(validToken, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_InvalidToken_ReturnsFalse() {
        // Arrange
        UserDetails userDetails = new User("not_valid", "pass", Collections.emptyList());

        // Act
        boolean isValid = jwtUtil.isTokenValid(validToken, userDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        UserDetails userDetails = new User("test", "pass", Collections.emptyList());

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void isTokenValid_TokenSignedWithDifferentKey_ReturnsFalse() {
        // Arrange
        UserDetails userDetails = new User("test", "pass", Collections.emptyList());

        SecretKey wrongKey = Keys.hmacShaKeyFor("different-secret-key-also-needs-32-bytes-minimum-length".getBytes());
        String wrongToken = Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION_MS))
                .signWith(wrongKey)
                .compact();

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.isTokenValid(wrongToken, userDetails));
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Act
        String username = jwtUtil.extractUsername(validToken);

        // Assert
        assertThat(username).isEqualTo(userDto.getUsername());
    }

    @Test
    void extractUsername_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername("invalid_token"));
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(expiredToken));
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpiration() {
        // Act
        Date expiration = jwtUtil.extractExpiration(validToken);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration.after(new Date())).isTrue();
    }

    @Test
    void extractExpiration_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractExpiration("invalid_token"));
        assertThrows(JwtException.class, () -> jwtUtil.extractExpiration(expiredToken));
    }

    @Test
    void extractAllClaims_ValidToken_ReturnsContent() {
        // Act
        Claims claims = jwtUtil.extractAllClaims(validToken);

        // Assert
        assertThat(claims.getSubject()).isEqualTo(userDto.getUsername());
        assertThat(claims.getIssuedAt()).isBefore(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void extractAllClaims_InvalidOrExpiredToken_Throws() {
        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims("invalid_token"));
        assertThrows(JwtException.class, () -> jwtUtil.extractAllClaims(expiredToken));
    }
}
