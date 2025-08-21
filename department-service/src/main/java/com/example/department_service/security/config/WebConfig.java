package com.example.department_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        // Frontend origin
        cors.setAllowedOrigins(List.of("http://localhost:3000"));
        // Frontend → Backend’da kullanacağımız HTTP metodları
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // JWT header’ı, JSON, vs.
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Tarayıcıya Authorization gibi header’ları göstermek isterseniz:
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Tüm endpoint’lere uygula
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
