package com.example.user_service.controller;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.AuthResponse;
import com.example.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Kimlik uçları:
 *  - POST /api/auth/register : Yeni kullanıcı yaratır, JWT döner
 *  - POST /api/auth/login    : Kullanıcı adı/şifre ile giriş, JWT döner
 *  - POST /api/auth/refresh  : Refresh token ile yeni access/refresh üretir
 *  - GET  /api/auth/me       : JWT'den kimliği okur
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Kayıt uç noktası */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** Giriş uç noktası */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Refresh token ile yeni tokenlar üretir */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /** Örnek korumalı endpoint: token'daki kullanıcıyı döndürür */
    @GetMapping("/me")
    public ResponseEntity<Object> me(Authentication auth) {
        // Authentication.getName() -> JwtAuthenticationFilter içinde set edilen "username"
        return ResponseEntity.ok(java.util.Map.of("username", auth.getName()));
    }
}
