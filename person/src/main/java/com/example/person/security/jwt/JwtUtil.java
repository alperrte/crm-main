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
        // Secret key HS256 algoritması için byte array'e çevrilir
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // issuer null ise boş string'e dönüştürülür
        this.issuer = issuer == null ? "" : issuer.trim();
        // roller için claim alan adı
        this.rolesClaim = rolesClaim;
    }

    // Gelen token'ı parse eder ve claim'leri döndürür
    public Claims parse(String token) {
        var builder = Jwts.parserBuilder().setSigningKey(key);
        // issuer tanımlıysa kontrol et
        if (!issuer.isBlank()) {
            builder.requireIssuer(issuer);
        }
        // Token parse edilip claim body döndürülür
        return builder.build().parseClaimsJws(token).getBody();
    }

    // Token içindeki rollerin listesi çıkarılır
    public List<String> extractRoles(Claims claims) {
        // Roller için olası claim isimlerini sırayla dene
        for (String name : List.of(rolesClaim, "roles", "role", "authorities")) {
            Object raw = claims.get(name);
            if (raw == null) continue;
            // Eğer roller String tipinde ise (ör: "USER")
            if (raw instanceof String s) {
                return List.of(s);
            }
            // Eğer roller Collection tipinde ise (ör: ["USER","ADMIN"])
            if (raw instanceof Collection<?> col) {
                List<String> out = new ArrayList<>(col.size());
                for (Object o : col) out.add(String.valueOf(o));
                return out;
            }
            // Eğer roller array tipinde ise (ör: String[])
            if (raw.getClass().isArray()) {
                Object[] arr = (Object[]) raw;
                List<String> out = new ArrayList<>(arr.length);
                for (Object o : arr) out.add(String.valueOf(o));
                return out;
            }
        }
        // Roller bulunamazsa boş liste döndür
        return List.of();
    }
}
