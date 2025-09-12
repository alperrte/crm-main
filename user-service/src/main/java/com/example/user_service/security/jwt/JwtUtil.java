package com.example.user_service.security.jwt;

import com.example.user_service.entity.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_ISSUER}")
    private String issuer;

    @Value("${JWT_EXPIRATION}") // saniye (1 saat)
    private long accessTtlSeconds;

    @Getter
    @Value("${JWT_REFRESH_EXPIRATION}") // saniye (7 gün)
    private long refreshTtlSeconds;

    // Anahtar üretimi (HS256)
    private Key key() {
        String s = secret;
        if (s == null || s.length() < 32) {
            s = (s == null ? "" : s) + "________________________________";
        }
        return Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8));
    }

    // Access token üretir
    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        if (user.getPersonId() != null) {
            claims.put("personId", user.getPersonId()); //
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) //
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh token üretir
    public String generateRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of("type", "refresh"))
                .setSubject(user.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token geçerli mi?
    public boolean isTokenValid(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Token geçersiz mi?
    public boolean isTokenInvalid(String token) {
        return !isTokenValid(token);
    }

    // Token'dan email bilgisini döndürür
    public String extractEmail(String token) {
        return getAllClaims(token).getSubject();
    }

    // Token'dan role bilgisini döndürür
    public String extractRole(String token) {
        Object role = getAllClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    // Token'dan personId bilgisini döndürür
    public Long extractPersonId(String token) {
        Object pid = getAllClaims(token).get("personId");
        return pid != null ? Long.parseLong(pid.toString()) : null;
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build();
    }

    // Tüm claim’ler
    private Claims getAllClaims(String token) {
        return parser().parseClaimsJws(token).getBody();
    }
}
