// src/main/java/com/example/person/security/jwt/JwtUtil.java
package com.example.person.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final String issuer;
    private final String rolesClaim;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer:}") String issuer,
            @Value("${jwt.roles-claim:roles}") String rolesClaim
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer == null ? "" : issuer.trim();
        this.rolesClaim = rolesClaim;
    }

    public Claims parse(String token) {
        var builder = Jwts.parserBuilder().setSigningKey(key);
        if (!issuer.isBlank()) {
            builder.requireIssuer(issuer);
        }
        return builder.build().parseClaimsJws(token).getBody();
    }

    public List<String> extractRoles(Claims claims) {
        for (String name : List.of(rolesClaim, "roles", "role", "authorities")) {
            Object raw = claims.get(name);
            if (raw == null) continue;

            if (raw instanceof String s) {
                return List.of(s);
            }
            if (raw instanceof Collection<?> col) {
                List<String> out = new ArrayList<>(col.size());
                for (Object o : col) out.add(String.valueOf(o));
                return out;
            }
            if (raw.getClass().isArray()) {
                Object[] arr = (Object[]) raw;
                List<String> out = new ArrayList<>(arr.length);
                for (Object o : arr) out.add(String.valueOf(o));
                return out;
            }
        }
        return List.of();
    }
}
