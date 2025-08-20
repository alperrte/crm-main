package com.example.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sadece email, username, password alınır.
 */
public record RegisterRequest(
        @Email(message="Geçerli bir email giriniz.") @NotBlank(message="Email zorunludur.") String email,
        @NotBlank(message="Kullanıcı adı zorunludur.") String username,
        @Size(min = 6, max = 72, message="Şifre en az 6 karakter olmalı (BCrypt limiti 72 byte).")
        @NotBlank(message="Şifre zorunludur.") String password
) {}
