package com.example.user_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String role;
    private Long personId;
    private String accessToken;
    private String refreshToken;
}
