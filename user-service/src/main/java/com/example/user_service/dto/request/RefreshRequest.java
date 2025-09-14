package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Refresh talebi
@Data
public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
