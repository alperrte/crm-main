package com.example.user_service.service;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.UserResponse;

public interface UserService {
    // Yeni bir USER rolüne sahip kullanıcı kaydı yapar.
    UserResponse registerUser(RegisterRequest request);

    // Yeni bir ADMIN rolüne sahip kullanıcı kaydı yapar.
    UserResponse registerAdmin(RegisterRequest request);

    // Yeni bir PERSON rolüne sahip kullanıcı kaydı yapar.
    UserResponse registerPerson(RegisterRequest request);

    // Kullanıcı girişi (email + şifre) yapar ve token üretir.
    UserResponse login(LoginRequest request);

    // Refresh token kullanarak yeni access token üretir.
    UserResponse refresh(RefreshRequest request);
}
