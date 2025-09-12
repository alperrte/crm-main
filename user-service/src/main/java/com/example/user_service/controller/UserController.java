package com.example.user_service.controller;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RefreshRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //ADMIN kaydı
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-admin")
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userService.refresh(request));
    }

    @GetMapping("/me")
    public ResponseEntity<Object> me(Authentication auth) {
        return ResponseEntity.ok(Map.of("username", auth.getName()));
    }
}
