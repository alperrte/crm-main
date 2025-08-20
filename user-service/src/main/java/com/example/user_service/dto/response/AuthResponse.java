package com.example.user_service.dto.response;

/**
 * Auth yanıtı: kullanıcı temel bilgileri + access/refresh token.
 */
public record AuthResponse(
        Long userId,
        String username,
        String role,
        Long personId,
        String accessToken,
        String refreshToken
) {}