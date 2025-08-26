package com.example.user_service.security.config;

import com.example.user_service.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(reg -> reg
                        // Preflight izin
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpointler
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/actuator",
                                "/actuator/**"
                        ).permitAll()

                        // Ticket açma: USER, PERSON, ADMIN
                        .requestMatchers("/tickets/create").hasAnyRole("USER", "PERSON", "ADMIN")

                        // Ticket yönetimi: PERSON, ADMIN
                        .requestMatchers("/tickets/**").hasAnyRole("PERSON", "ADMIN")

                        // Admin işlemleri
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // diğer her şey auth ister
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(SecurityConfig::unauthorizedEntryPoint)
                        .accessDeniedHandler(SecurityConfig::forbiddenHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void unauthorizedEntryPoint(
            HttpServletRequest req,
            HttpServletResponse res,
            AuthenticationException ex
    ) throws IOException {
        String msg = (ex.getMessage() != null ? ex.getMessage() : "Authentication required")
                + " @ " + req.getMethod() + " " + req.getRequestURI();
        writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", msg);
    }

    private static void forbiddenHandler(
            HttpServletRequest req,
            HttpServletResponse res,
            AccessDeniedException ex
    ) throws IOException {
        String msg = (ex.getMessage() != null ? ex.getMessage() : "Access denied")
                + " @ " + req.getMethod() + " " + req.getRequestURI();
        writeJson(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden", msg);
    }

    private static void writeJson(HttpServletResponse res, int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        String body = "{\"error\":\"" + escape(error) + "\",\"message\":\"" + escape(message) + "\"}";
        res.getWriter().write(body);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
