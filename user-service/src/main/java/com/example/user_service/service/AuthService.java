package com.example.user_service.service;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest request);   // public
    AuthResponse registerAdmin(RegisterRequest request);  // sadece ADMIN
    AuthResponse registerPerson(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
}