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

/**
 * Kayıt, giriş ve refresh akışlarının gerçeklenmesi.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** Yeni kullanıcı yaratır ve token döner */
    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.username());
        }

        // BCrypt 72 byte limiti kontrolü (özellikle çok uzun şifrelerde hata önlemek için)
        if (request.password().getBytes().length > 72) {
            throw new IllegalArgumentException("password cannot be more than 72 bytes");
        }

        UserEntity user = UserEntity.builder()
                .username(request.username()) // Entity alanı ile DB kolonu uyumlu
                .passwordHash(passwordEncoder.encode(request.password()))
                .role((request.role() == null || request.role().isBlank()) ? "ROLE_USER" : request.role())
                .personId(request.personId())
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    /** Kullanıcı adı/şifre ile giriş yapar ve token döner */
    @Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // BCrypt 72 byte limiti kontrolü
        if (request.password().getBytes().length > 72) {
            throw new IllegalArgumentException("password cannot be more than 72 bytes");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Kullanıcı adı veya şifre hatalı");
        }

        return generateTokensAndSave(user);
    }

    /**
     * Refresh token doğrulama ve rotate etme.
     * - Gönderilen refreshToken imzalı ve süresi geçmemiş olmalı
     * - DB'deki kayıt ile eşleşmeli (plain text karşılaştırma)
     * - Başarılıysa yeni access+refresh üretip DB'ye yeni değer ve son kullanma tarihi yazılır
     */
    @Transactional
    @Override
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        // 1) İmza ve zaman kontrolü
        if (jwtUtil.isTokenInvalid(token)) {
            throw new IllegalArgumentException("Geçersiz veya süresi geçmiş refresh token");
        }

        String username = jwtUtil.extractUsername(token);

        // 2) Kullanıcıyı çek
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // 3) DB'de saklı refresh token ve son kullanma ile doğrula
        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpires() == null) {
            throw new IllegalArgumentException("Refresh token kayıtlı değil");
        }
        if (LocalDateTime.now().isAfter(user.getRefreshTokenExpires())) {
            throw new IllegalArgumentException("Refresh token süresi dolmuş");
        }
        // ⬇ BCrypt yerine plain equals ile karşılaştırıyoruz, böylece 72 byte sınırı kalkıyor
        if (!token.equals(user.getRefreshTokenHash())) {
            throw new IllegalArgumentException("Refresh token eşleşmedi (rotated veya invalid)");
        }

        // 4) Yeni tokenlar üret ve DB'ye yaz
        return generateTokensAndSave(user);
    }

    /** Token üretimi + refresh token/expires güncelleme */
    private AuthResponse generateTokensAndSave(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // ⬇ BCrypt yerine plain text saklıyoruz, böylece 72 byte limiti hatası ortadan kalkar
        user.setRefreshTokenHash(refreshToken);
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