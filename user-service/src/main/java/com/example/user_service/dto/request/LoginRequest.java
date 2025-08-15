package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Login isteği için DTO.
 * - username + password
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
