package com.example.ticket_service.security.config;

import com.example.ticket_service.security.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔹 Preflight OPTIONS (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔹 Public endpointler
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers("/api/tickets/public/**").permitAll()

                        // 🔹 Admin uçları → sadece ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 🔹 Departman uçları → ADMIN + PERSON
                        .requestMatchers("/api/departments/**").hasAnyRole("ADMIN", "PERSON")

                        // 🔹 User panel uçları → sadece USER
                        .requestMatchers("/api/user-panel/**").hasRole("USER")

                        // 🔹 Ticket oluşturma → USER, PERSON, ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/tickets/create")
                        .hasAnyRole("USER", "PERSON", "ADMIN")

                        // 🔹 Ticket listeleme & işlemleri → PERSON + ADMIN
                        .requestMatchers("/api/tickets/**").hasAnyRole("PERSON", "ADMIN")

                        // 🔹 Geri kalan her şey → login gerekli
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
