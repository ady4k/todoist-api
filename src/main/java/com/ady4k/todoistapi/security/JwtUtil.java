package com.ady4k.todoistapi.security;

import com.ady4k.todoistapi.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

@UtilityClass
public class JwtUtil {
    private static final String KEY = System.getenv("SECRET_KEY");
    private static final int EXPIRY_TIME = 1000 * 60 * 60;

    public String generateToken(UserDto userDto) {
        return Jwts.builder()
                .subject(userDto.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRY_TIME))
                .signWith(getSigningKey())
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
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw new JwtException(ex.getMessage());
        }
        return jws;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(KEY.getBytes());
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
