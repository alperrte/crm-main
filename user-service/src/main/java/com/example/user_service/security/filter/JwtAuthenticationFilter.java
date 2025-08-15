package com.example.user_service.security.filter;

import com.example.user_service.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authorization: Bearer <token> başlığını okuyup
 * doğrulanmışsa SecurityContext'e kimliği yerleştirir.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Header yoksa veya Bearer değilse -> devam
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Zaten kimlik set edilmişse tekrar doğrulamayız
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token geçersiz ise -> devam (401 üretmeyi config’e bırakırız)
        if (jwtUtil.isTokenInvalid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Geçerli -> kullanıcıyı context'e koy
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        List<SimpleGrantedAuthority> authorities =
                (role != null) ? List.of(new SimpleGrantedAuthority(role))
                        : List.of();

        var authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
