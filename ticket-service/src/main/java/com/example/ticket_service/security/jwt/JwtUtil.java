    // src/main/java/com/example/ticket_service/security/jwt/JwtUtil.java
    package com.example.ticket_service.security.jwt;

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

    /**
     * - HS256 secret'ı .yml/.env'den okur
     * - issuer belirtilmişse doğrular (boş bırakılırsa kontrol etmez)
     * - rolleri şu claim adlarından ve formatlardan okur:
     *   roles-claim (yml'deki), "roles", "role", "authorities"
     *   String  -> "ADMIN"
     *   List    -> ["ADMIN", "EMPLOYEE"]
     *   Array   -> ["ADMIN", ...]
     */
    @Component
    public class JwtUtil {

        private final SecretKey key;
        private final String issuer;      // boş ise issuer kontrolü YOK
        private final String rolesClaim;  // öncelikli claim adı (yml'den)

        public JwtUtil(
                @Value("${jwt.secret}") String secret,
                @Value("${jwt.issuer:}") String issuer,
                @Value("${jwt.roles-claim:roles}") String rolesClaim
        ) {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.issuer = issuer == null ? "" : issuer.trim();
            this.rolesClaim = rolesClaim;
        }

        /** Geçerli imzalı JWT'yi parse eder; issuer boş değilse doğrular. */
        public Claims parse(String token) {
            var builder = Jwts.parserBuilder().setSigningKey(key);
            if (!issuer.isBlank()) {
                builder.requireIssuer(issuer);
            }
            return builder.build().parseClaimsJws(token).getBody();
        }

        /** roles|role|authorities + String/List/Array hepsini destekler. */
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
            return List.of(); // rol bulunamazsa boş döner (JwtFilter'da fallback uygulayacağız)
        }
    }
