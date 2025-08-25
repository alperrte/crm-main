package com.example.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Kullanıcı kaydı için DTO.
 */
// 🔹 Burayı güncelledik: name, surname, phone eklendi
public record RegisterRequest(
        @NotBlank(message="Ad zorunludur.") String name,
        @NotBlank(message="Soyad zorunludur.") String surname,
        @Email(message="Geçerli bir email giriniz.") @NotBlank(message="Email zorunludur.") String email,
        @NotBlank(message="Telefon zorunludur.") String phone,
        @Size(min = 6, max = 72, message="Şifre en az 6 karakter olmalı (BCrypt limiti 72 byte).")
        @NotBlank(message="Şifre zorunludur.") String password
) {}
