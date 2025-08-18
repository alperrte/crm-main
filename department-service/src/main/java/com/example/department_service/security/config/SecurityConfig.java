package com.example.department_service.security.config;

import com.example.department_service.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                // CSRF kapalı (REST API olduğu için)
                .csrf(AbstractHttpConfigurer::disable)

                // Session yok → tamamen stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Endpoint kuralları
                .authorizeHttpRequests(auth -> auth
                        // Actuator health/info → serbest
                        .requestMatchers("/actuator/**").permitAll()
                        // Department API → JWT zorunlu
                        .requestMatchers("/api/department/**").authenticated()
                        // Diğer tüm istekler → JWT zorunlu
                        .anyRequest().authenticated()
                )

                // JWT filter’ı username/password filtresinden önce devreye al
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
