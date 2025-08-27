package com.example.ticket_service.security.filter;

import com.example.ticket_service.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        log.debug("➡️ Request geldi: {} {}", req.getMethod(), path);

        // Public endpointler → token gerektirmez
        if (path.startsWith("/api/tickets/public")
                || path.startsWith("/api/categories")
                || path.startsWith("/actuator")) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwt.parse(token);
                var roles = jwt.extractRoles(claims);

                log.info("🔑 JWT subject: {}", claims.getSubject());
                log.info("🔑 Extracted roles: {}", roles);

                // ❌ rol yoksa → login başarısız
                if (roles == null || roles.isEmpty()) {
                    log.warn("⚠️ Kullanıcının rolü yok → giriş reddedildi!");
                    SecurityContextHolder.clearContext();
                    chain.doFilter(req, res);
                    return;
                }

                var auths = roles.stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                token,
                                auths
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("✅ SecurityContext set edildi: user={}, authorities={}",
                        claims.getSubject(), auths);

            } catch (Exception e) {
                log.error("❌ JWT parse/validate hatası", e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("⚠️ Authorization header yok veya Bearer başlamıyor!");
        }

        chain.doFilter(req, res);
    }
}
