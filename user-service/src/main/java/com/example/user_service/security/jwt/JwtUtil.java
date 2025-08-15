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
import java.util.Map;

/**
 * JWT üretim / doğrulama yardımcı sınıfı.
 * - HS256 imza
 * - subject: username
 * - claim'ler: userId, role
 */
@Component
public class JwtUtil {

    @Value("${JWT_SECRET:change_me_min_32_chars________________}")
    private String secret;

    @Value("${JWT_ISSUER:user-service}")
    private String issuer;

    @Value("${JWT_EXPIRATION:3600}") // 1 saat
    private long accessTtlSeconds;

    @Getter
    @Value("${JWT_REFRESH_EXPIRATION:604800}") // 7 gün
    private long refreshTtlSeconds;

    /**
     * Anahtar üretimi (HS256)
     * Secret 32 karakterden kısa ise otomatik olarak doldurulur.
     */
    private Key key() {
        String s = secret;
        if (s == null || s.length() < 32) {
            s = (s == null ? "" : s) + "________________________________";
        }
        return Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8));
    }

    /** Access token üretir */
    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of(
                        "userId", user.getId(),
                        "role", user.getRole()
                ))
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh token üretir */
    public String generateRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of("type", "refresh"))
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Token geçerli mi? */
    public boolean isTokenValid(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Token geçersiz mi? (Daha okunaklı ters kontrol için eklendi) */
    public boolean isTokenInvalid(String token) {
        return !isTokenValid(token);
    }

    /** Token'dan username bilgisini döndürür */
    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    /** Token'dan role bilgisini döndürür */
    public String extractRole(String token) {
        Object role = getAllClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    /** JWT parser oluşturur */
    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build();
    }

    /** Token içindeki tüm claim’leri döndürür */
    private Claims getAllClaims(String token) {
        return parser().parseClaimsJws(token).getBody();
    }
}
