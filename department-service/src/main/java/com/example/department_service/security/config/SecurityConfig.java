package com.example.department_service.security.config;

import com.example.department_service.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> {}) // CORS aktif
                .csrf(csrf -> csrf.disable()) // CSRF kapalı

                // Session yok → tamamen stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Endpoint kuralları
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll() // health/info serbest
                        .requestMatchers("/api/departments/**").authenticated() // ✅ artık doğru endpoint
                        .anyRequest().authenticated()
                )

                // JWT filter’ı username/password filtresinden önce devreye al
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
