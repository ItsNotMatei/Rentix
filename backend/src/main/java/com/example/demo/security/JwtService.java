package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, 32);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        UserRole role = UserRole.fromString(user.getRole());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .claims(Map.of(
                        "userId", user.getId(),
                        "role", role.name(),
                        "type", "access"
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpirationMs()))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID();
    }

    public boolean isTokenValid(String token, String email) {
        try {
            String subject = extractClaim(token, Claims::getSubject);
            return subject.equals(email) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(extractClaim(token, claims -> claims.get("type", String.class)));
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public UserRole extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return UserRole.fromString(role);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
