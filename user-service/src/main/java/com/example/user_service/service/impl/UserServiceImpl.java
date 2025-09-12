package com.example.user_service.service.impl;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.jwt.JwtUtil;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    @Override
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.getEmail());
        if (request.getPassword().getBytes().length > 72)
            throw new IllegalArgumentException("Password cannot be more than 72 bytes");
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public UserResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.getEmail());
        if (request.getPassword().getBytes().length > 72)
            throw new IllegalArgumentException("Password cannot be more than 72 bytes");
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN")
                .build();
        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public UserResponse registerPerson(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email zaten kayıtlı: " + request.getEmail());
        if (request.getPassword().getBytes().length > 72)
            throw new IllegalArgumentException("Password cannot be more than 72 bytes");
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("PERSON")
                .build();
        userRepository.save(user);
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public UserResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        if (request.getPassword().getBytes().length > 72)
            throw new IllegalArgumentException("Password cannot be more than 72 bytes");
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("Email veya şifre hatalı");
        return generateTokensAndSave(user);
    }

    @Transactional
    @Override
    public UserResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        if (jwtUtil.isTokenInvalid(token))
            throw new IllegalArgumentException("Geçersiz veya süresi geçmiş refresh token");
        String email = jwtUtil.extractEmail(token);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpires() == null)
            throw new IllegalArgumentException("Refresh token kayıtlı değil");
        if (LocalDateTime.now().isAfter(user.getRefreshTokenExpires()))
            throw new IllegalArgumentException("Refresh token süresi dolmuş");
        if (!token.equals(user.getRefreshTokenHash()))
            throw new IllegalArgumentException("Refresh token eşleşmedi (rotated/invalid)");
        return generateTokensAndSave(user);
    }

    private UserResponse generateTokensAndSave(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshTokenHash(refreshToken);
        user.setRefreshTokenExpires(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTtlSeconds()));
        userRepository.save(user);
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .role(user.getRole())
                .personId(user.getPersonId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
