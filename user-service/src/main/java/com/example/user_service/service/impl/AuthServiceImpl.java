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
        if (userRepository.existsByUsername(request.email()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.email());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        // 🔹 Burayı güncelledik: name, surname, phone eklendi
        UserEntity user = UserEntity.builder()
                .username(request.email()) // username'i email ile eşledik
                .name(request.name())
                .surname(request.surname())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByUsername(request.email()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.email());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        // 🔹 Burayı güncelledik: name, surname, phone eklendi
        UserEntity user = UserEntity.builder()
                .username(request.email())
                .name(request.name())
                .surname(request.surname())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("ADMIN")
                .build();

        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public AuthResponse registerPerson(RegisterRequest request) {
        if (userRepository.existsByUsername(request.email()))
            throw new IllegalArgumentException("Kullanıcı adı zaten mevcut: " + request.email());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.email());

        if (request.password().getBytes().length > 72)
            throw new IllegalArgumentException("password cannot be more than 72 bytes");

        // 🔹 Burayı güncelledik: name, surname, phone eklendi
        UserEntity user = UserEntity.builder()
                .username(request.email())
                .name(request.name())
                .surname(request.surname())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("PERSON")
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

        if (jwtUtil.isTokenInvalid(token))
            throw new IllegalArgumentException("Geçersiz veya süresi geçmiş refresh token");

        String username = jwtUtil.extractUsername(token);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpires() == null)
            throw new IllegalArgumentException("Refresh token kayıtlı değil");

        if (LocalDateTime.now().isAfter(user.getRefreshTokenExpires()))
            throw new IllegalArgumentException("Refresh token süresi dolmuş");

        if (!token.equals(user.getRefreshTokenHash()))
            throw new IllegalArgumentException("Refresh token eşleşmedi (rotated/invalid)");

        return generateTokensAndSave(user);
    }

    private AuthResponse generateTokensAndSave(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

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
