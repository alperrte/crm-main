package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Refresh talebi. Body: { "refreshToken": "..." } */
public record RefreshRequest(@NotBlank String refreshToken) {}
