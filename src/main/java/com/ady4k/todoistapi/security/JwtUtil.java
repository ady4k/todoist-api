package com.ady4k.todoistapi.security;

import com.ady4k.todoistapi.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final String secretKey;
    private final long expirationTimeMinutes;

    private SecretKey signingKey;

    public JwtUtil(@Value("${token.secret}")String secretKey,
                   @Value("${token.expiration.time.minutes}") long expirationTimeMinutes) {
        this.secretKey = secretKey;
        this.expirationTimeMinutes = expirationTimeMinutes;
    }

    @PostConstruct
    public void init() {
        log.debug("Initializing JwtUtil with secret key: {}", secretKey);
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        log.info("JWT Util initialized successfully with expiration time: {} minutes", expirationTimeMinutes);
    }

    public String generateToken(UserDto userDto) {
        return Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTimeMinutes))
                .signWith(this.signingKey)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Claims extractAllClaims(String token) throws JwtException {
        Claims jws;
        try {
            jws = Jwts.parser()
                    .verifyWith(this.signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new JwtException(ex.getMessage());
        }
        return jws;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
