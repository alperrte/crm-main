package com.example.person.security.filter;

import com.example.person.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        if (path.startsWith("/actuator")) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwt.parse(token);

                // 🔑 Roller
                var roles = jwt.extractRoles(claims);
                if (roles == null || roles.isEmpty()) {
                    roles = List.of("ADMIN"); // fallback
                }

                var auths = roles.stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 🔑 Principal olarak email/sub set et
                String email = claims.getSubject();

                // Ek metadata
                Map<String, Object> details = new HashMap<>();
                if (claims.get("deptId") != null) {
                    details.put("deptId", claims.get("deptId"));
                }
                if (claims.get("name") != null) {
                    details.put("name", claims.get("name"));
                }
                if (claims.get("surname") != null) {
                    details.put("surname", claims.get("surname"));
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(email, null, auths);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // Ek detayları ayrıca kaydet
                authentication.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(req, res);
    }
}
