// src/main/java/com/example/user_service/service/impl/AuthServiceImpl.java
package com.example.user_service.service.impl;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.AuthResponse;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.jwt.JwtUtil;
import com.example.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        // Benzersizlik
        if (userRepository.existsByUsername(request.username()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.username());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        // BCrypt 72 byte limiti
        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")           // public register her zaman USER
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse registerAdmin(RegisterRequest request) {
        // Sadece admin endpoint’inden çağrılacak (Controller katmanında @PreAuthorize ile korunuyor)

        if (userRepository.existsByUsername(request.username()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.username());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("ADMIN")          // admin oluşturma
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse registerPerson(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.username());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("PERSON")   // 👈 çalışan kaydı
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("Kullanıcı adı veya şifre hatalı");

        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        // 1) İmza/süre kontrolü
        if (jwtUtil.isTokenInvalid(token))
            throw new IllegalArgumentException("Geçersiz veya süresi geçmiş refresh token");

        String username = jwtUtil.extractUsername(token);

        // 2) Kullanıcıyı bul
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // 3) DB kayıtları ile doğrula
        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpires() == null)
            throw new IllegalArgumentException("Refresh token kayıtlı değil");

        if (LocalDateTime.now().isAfter(user.getRefreshTokenExpires()))
            throw new IllegalArgumentException("Refresh token süresi dolmuş");

        if (!token.equals(user.getRefreshTokenHash()))
            throw new IllegalArgumentException("Refresh token eşleşmedi (rotated/invalid)");

        // 4) Yeni tokenlar
        return generateTokensAndSave(user);
    }

    private AuthResponse generateTokensAndSave(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        user.setRefreshTokenHash(refreshToken); // plain saklıyoruz (72 byte limiti derdi yok)
        user.setRefreshTokenExpires(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTtlSeconds()));
        userRepository.save(user);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getPersonId(),
                accessToken,
                refreshToken
        );
    }
}