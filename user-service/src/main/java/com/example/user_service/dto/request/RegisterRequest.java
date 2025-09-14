package com.example.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Ad zorunludur.")
    private String name;
    @NotBlank(message = "Soyad zorunludur.")
    private String surname;
    @NotBlank(message = "Email zorunludur.")
    @Email(message = "Geçerli bir email giriniz.")
    private String email;
    @NotBlank(message = "Telefon zorunludur.")
    private String phone;
    @NotBlank(message = "Şifre zorunludur.")
    @Size(min = 6, max = 72, message = "Şifre en az 6 karakter olmalı (BCrypt limiti 72 byte).")
    private String password;
}
