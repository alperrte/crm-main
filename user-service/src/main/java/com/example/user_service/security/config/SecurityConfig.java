package com.example.user_service.security.config;

import com.example.user_service.security.filter.JwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF devre dışı bırakıldı
                .csrf(AbstractHttpConfigurer::disable)
                // CORS desteği açıldı
                .cors(cors -> {})
                // Endpoint bazlı yetkilendirme kuralları
                .authorizeHttpRequests(reg -> reg
                        // Preflight (OPTIONS) isteklerine izin ver
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpointler
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/actuator",
                                "/actuator/**"
                        ).permitAll()
                        // Ticket oluşturma → USER, PERSON, ADMIN rolleri erişebilir
                        .requestMatchers("/tickets/create").hasAnyRole("USER", "PERSON", "ADMIN")
                        // Ticket yönetimi → sadece PERSON ve ADMIN erişebilir
                        .requestMatchers("/tickets/**").hasAnyRole("PERSON", "ADMIN")
                        // Admin paneli → sadece ADMIN erişebilir
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Yukarıdakilerin dışında kalan her endpoint → authentication zorunlu
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Basic Auth ve form login kapatıldı (sadece JWT kullanılacak)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(eh -> eh
                        // Kimlik doğrulaması yoksa → 401 Unauthorized
                        .authenticationEntryPoint(SecurityConfig::unauthorizedEntryPoint)
                        // Yetki yoksa → 403 Forbidden
                        .accessDeniedHandler(SecurityConfig::forbiddenHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Şifreleri güvenli saklamak için BCrypt encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 401 Unauthorized handler
    private static void unauthorizedEntryPoint(
            HttpServletRequest req,
            HttpServletResponse res,
            AuthenticationException ex
    ) throws IOException {
        String msg = (ex.getMessage() != null ? ex.getMessage() : "Authentication required")
                + " @ " + req.getMethod() + " " + req.getRequestURI();
        writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", msg);
    }

    // 403 Forbidden handler
    private static void forbiddenHandler(
            HttpServletRequest req,
            HttpServletResponse res,
            AccessDeniedException ex
    ) throws IOException {
        String msg = (ex.getMessage() != null ? ex.getMessage() : "Access denied")
                + " @ " + req.getMethod() + " " + req.getRequestURI();
        writeJson(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden", msg);
    }

    // Hata cevaplarını JSON formatında yazdıran yardımcı metod
    private static void writeJson(HttpServletResponse res, int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        String body = "{\"error\":\"" + escape(error) + "\",\"message\":\"" + escape(message) + "\"}";
        res.getWriter().write(body);
    }
    // JSON içinde kaçış karakterlerini düzgün göstermek için yardımcı metod
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
